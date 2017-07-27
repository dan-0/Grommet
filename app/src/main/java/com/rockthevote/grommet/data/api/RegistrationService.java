package com.rockthevote.grommet.data.api;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.NetworkChangeReceiver;
import com.rockthevote.grommet.data.api.model.ApiAdditionalInfo;
import com.rockthevote.grommet.data.api.model.ApiAddress;
import com.rockthevote.grommet.data.api.model.ApiContactMethod;
import com.rockthevote.grommet.data.api.model.ApiGeoLocation;
import com.rockthevote.grommet.data.api.model.ApiName;
import com.rockthevote.grommet.data.api.model.ApiRegistrationHelper;
import com.rockthevote.grommet.data.api.model.ApiRockyRequest;
import com.rockthevote.grommet.data.api.model.ApiRockyRequestWrapper;
import com.rockthevote.grommet.data.api.model.ApiSignature;
import com.rockthevote.grommet.data.api.model.ApiVoterClassification;
import com.rockthevote.grommet.data.api.model.ApiVoterId;
import com.rockthevote.grommet.data.api.model.ApiVoterRecordsRequest;
import com.rockthevote.grommet.data.api.model.ApiVoterRegistration;
import com.rockthevote.grommet.data.api.model.ClockInRequest;
import com.rockthevote.grommet.data.api.model.ClockOutRequest;
import com.rockthevote.grommet.data.db.model.AdditionalInfo;
import com.rockthevote.grommet.data.db.model.Address;
import com.rockthevote.grommet.data.db.model.ContactMethod;
import com.rockthevote.grommet.data.db.model.Name;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.rockthevote.grommet.data.db.model.Session;
import com.rockthevote.grommet.data.db.model.VoterClassification;
import com.rockthevote.grommet.data.db.model.VoterId;
import com.rockthevote.grommet.ui.UploadNotification;
import com.rockthevote.grommet.util.Dates;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.rockthevote.grommet.data.db.model.Address.Type.ASSISTANT_ADDRESS;
import static com.rockthevote.grommet.data.db.model.Address.Type.MAILING_ADDRESS;
import static com.rockthevote.grommet.data.db.model.Address.Type.PREVIOUS_ADDRESS;
import static com.rockthevote.grommet.data.db.model.Address.Type.REGISTRATION_ADDRESS;
import static com.rockthevote.grommet.data.db.model.ContactMethod.Type.ASSISTANT_PHONE;
import static com.rockthevote.grommet.data.db.model.Name.Type.ASSISTANT_NAME;
import static com.rockthevote.grommet.data.db.model.Name.Type.CURRENT_NAME;
import static com.rockthevote.grommet.data.db.model.Name.Type.PREVIOUS_NAME;
import static com.rockthevote.grommet.data.db.model.RockyRequest.GENERATED_DATE;
import static com.rockthevote.grommet.data.db.model.RockyRequest.STATUS;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.ABANDONED;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.FORM_COMPLETE;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.IN_PROGRESS;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.REGISTER_CLIENT_FAILURE;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.REGISTER_SERVER_FAILURE;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.REGISTER_SUCCESS;
import static java.util.Map.Entry;

public class RegistrationService extends Service {

    @SuppressWarnings("WeakerAccess")
    @Inject BriteDatabase db;

    @SuppressWarnings("WeakerAccess")
    @Inject RockyService rockyService;

    private final AtomicInteger regRefCount = new AtomicInteger(0);
    private final AtomicInteger regTotalCount = new AtomicInteger(0);

    private final AtomicInteger inRefCount = new AtomicInteger(0);
    private final AtomicInteger inTotalCount = new AtomicInteger(0);

    private final AtomicInteger outRefCount = new AtomicInteger(0);
    private final AtomicInteger outTotalCount = new AtomicInteger(0);

    @Override
    public void onCreate() {
        Injector.obtain(getApplicationContext()).inject(this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("RegistrationService starting");
        cleanup();
        doWorkIfNeeded();
        return START_STICKY;
    }

    private void doWorkIfNeeded() {

        if (isConnected()) {

            uploadRegistrationsIfNeeded();
            reportClockInIfNeeded();
            reportClockOutIfNeeded();

        } else {
            Timber.d("RegistrationService stopping: no wifi");

            // if we don't have connectivity start a receiver to listen for connectivity change
            // and restart this service
            Context context = getApplicationContext();
            ComponentName receiver = new ComponentName(context, NetworkChangeReceiver.class);
            PackageManager pm = context.getPackageManager();

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

            stopSelf();
        }
    }

    private void shouldStop() {
        if (regRefCount.get() == regTotalCount.get()
                && inRefCount.get() == inTotalCount.get()
                && outRefCount.get() == outTotalCount.get()) {

            stopSelf();
        }
    }

    private void uploadRegistrationsIfNeeded() {

        Cursor cursor = db.query(RockyRequest.SELECT_BY_STATUS, FORM_COMPLETE.toString());
        int rows = cursor.getCount();
        cursor.close();
        regTotalCount.set(rows);

        if (rows > 0) {
            // check for registrations to upload
            db.createQuery(RockyRequest.TABLE,
                    RockyRequest.SELECT_BY_STATUS, FORM_COMPLETE.toString())
                    .mapToList(RockyRequest.MAPPER)
                    .flatMap(Observable::from)
                    .take(rows)
                    .subscribe(this::uploadRegistration);
        } else {
            Timber.d("RegistrationService stopping: no rows to upload");
            shouldStop();
        }
    }

    private void reportClockInIfNeeded() {
        // check to see if there are any clock out reports to send
        Cursor cursor = db.query(Session.SELECT_UNREPORTED_CLOCK_IN);
        int rows = cursor.getCount();
        cursor.close();
        inTotalCount.set(rows);

        if (rows > 0) {
            // check for clock-in requests to upload
            db.createQuery(Session.TABLE,
                    Session.SELECT_UNREPORTED_CLOCK_IN)
                    .mapToList(Session.MAPPER)
                    .flatMap(Observable::from)
                    .take(rows)
                    .subscribe(this::reportClockIn,
                            throwable -> Timber.d(throwable, "report clock in error"));
        } else {
            shouldStop();
        }
    }

    private void reportClockOutIfNeeded() {
        // check to see if there are any clock out reports to send
        Cursor cursor = db.query(Session.SELECT_UNREPORTED_CLOCK_IN);
        int rows = cursor.getCount();
        cursor.close();
        outTotalCount.set(rows);

        if (rows > 0) {
            db.createQuery(Session.TABLE,
                    Session.SELECT_UNREPORTED_CLOCK_OUT)
                    .mapToList(Session.MAPPER)
                    .flatMap(Observable::from)
                    .take(rows)
                    .subscribe(this::reportClockOut,
                            throwable -> Timber.d(throwable, "report clock out error"));
        } else {
            shouldStop();
        }
    }

    private boolean isConnected() {
        // check to make sure we have internet
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void reportClockIn(final Session session) {
        ClockInRequest request = ClockInRequest.builder()
                .canvasserName(session.canvasserName())
                .clockInDatetime(Dates.formatAsISO8601_Date(session.clockInTime()))
                .geoLocation(ApiGeoLocation.builder()
                        .latitude(session.latitude())
                        .longitude(session.longitude())
                        .build())
                .partnerTrackingId(session.partnerTrackingId())
                .sourceTrackingId(session.sourceTrackingId())
                .openTrackingId(session.openTrackingId())
                .sessionTimeoutLength(session.sessionTimeout())
                .build();

        rockyService.clockIn(request)
                .subscribeOn(Schedulers.io())
                .doOnCompleted(() -> {
                    /*
                    because this method processes each row in the initial query one-at-a-time
                    we need to check after each row finishes to see if we should stop the service
                    */
                    if (inRefCount.incrementAndGet() == inTotalCount.get()) {
                        Timber.d("Finished reporting clock-ins");
                        shouldStop();
                    }
                })
                .subscribe(result ->
                {
                    Timber.d("reporting clock in");

                    if (!result.isError() && result.response().isSuccessful()) {
                        db.update(Session.TABLE,
                                new Session.Builder()
                                        .clockInReported(true)
                                        .build(),
                                Session._ID + " = ? ", String.valueOf(session.id()));
                    }
                });
    }

    private void reportClockOut(final Session session) {
        ClockOutRequest request = ClockOutRequest.builder()
                .canvasserName(session.canvasserName())
                .clockOutDatetime(Dates.formatAsISO8601_Date(session.clockOutTime()))
                .geoLocation(ApiGeoLocation.builder()
                        .latitude(session.latitude())
                        .longitude(session.longitude())
                        .build())
                .partnerTrackingId(session.partnerTrackingId())
                .sourceTrackingId("")
                .openTrackingId("")
                .sessionTimeoutLength(session.sessionTimeout())
                .build();

        rockyService.clockOut(request)
                .subscribeOn(Schedulers.io())
                .doOnCompleted(() -> {
                    /*
                    because this method processes each row in the initial query one-at-a-time
                    we need to check after each row finishes to see if we should stop the service
                    */
                    if (outRefCount.incrementAndGet() == outTotalCount.get()) {
                        Timber.d("Finished reporting clock-outs");
                        shouldStop();
                    }
                })
                .subscribe(result ->
                {
                    Timber.d("reporting clock out");

                    if (!result.isError() && result.response().isSuccessful()) {
                        db.update(Session.TABLE,
                                new Session.Builder()
                                        .clockOutReported(true)
                                        .build(),
                                Session._ID + " = ? ", String.valueOf(session.id()));
                    }
                });
    }

    private void uploadRegistration(final RockyRequest rockyRequest) {
        if (null == rockyRequest) {
            return;
        }

        getApiRockyRequest(rockyRequest) // returns Observable<ApiRockyRequest>
                .flatMap(apiRockyRequestWrapper -> rockyService.register(apiRockyRequestWrapper))
                .subscribeOn(Schedulers.io())
                .doOnCompleted(() -> {
                    /*
                    because this method processes each row in the initial query one-at-a-time
                    we need to check after each row finishes to see if we should stop the service
                    */
                    if (regRefCount.incrementAndGet() == regTotalCount.get()) {
                        Timber.d("RegistrationService stopping: work complete");
                        shouldStop();
                    }
                })
                .subscribe(regResponse ->
                        {
                            if (regResponse.isError()) {
                                // there was an error contacting the server, don't delete the row
                                UploadNotification.notify(getApplicationContext(), REGISTER_SERVER_FAILURE);
                            } else {

                                RockyRequest.Status status;

                                if (regResponse.response().isSuccessful()) {
                                    status = REGISTER_SUCCESS;
                                } else {
                                    int code = regResponse.response().code();
                                    if (code < 500) {
                                        status = REGISTER_CLIENT_FAILURE;
                                    } else {
                                        status = REGISTER_SERVER_FAILURE;
                                    }
                                }
                                updateRegistrationStatus(status, rockyRequest.id());
                            }
                        },
                        throwable -> {
                            // mark the row for removal if the data is corrupt
                            // this is a client error, meaning the network request was never made
                            updateRegistrationStatus(REGISTER_CLIENT_FAILURE, rockyRequest.id());
                        }
                );
    }

    private void updateRegistrationStatus(RockyRequest.Status status, long rowId) {
        UploadNotification.notify(getApplicationContext(), status);
        db.update(RockyRequest.TABLE,
                new RockyRequest.Builder()
                        .status(status)
                        .build(),
                RockyRequest._ID + " = ? ", String.valueOf(rowId));
    }

    /**
     * check for <p>
     * {@link RockyRequest.Status#ABANDONED},<p>
     * {@link RockyRequest.Status#REGISTER_SERVER_FAILURE},<p>
     * {@link RockyRequest.Status#REGISTER_SUCCESS} <p>
     * as well as {@link RockyRequest.Status#IN_PROGRESS} that are more than one hour old
     * rows and delete them
     */
    @SuppressLint("BinaryOperationInTimber") // it's addition, not concatenation
    private void cleanup() {
        String completedRows = ""
                + STATUS + " IN ("
                + "'" + ABANDONED + "', "
                + "'" + REGISTER_SERVER_FAILURE + "',"
                + "'" + REGISTER_CLIENT_FAILURE + "',"
                + "'" + REGISTER_SUCCESS + "'"
                + ")";

        String oldRows = ""
                + STATUS + " = '" + IN_PROGRESS + "'"
                + " AND "
                + " datetime(" + GENERATED_DATE + ") <= datetime('now','-1 hour')";


        int numCompletedRows = db.delete(RockyRequest.TABLE, completedRows);
        int numOldRows = db.delete(RockyRequest.TABLE, oldRows);

        Timber.d("%s Rows were deleted", numCompletedRows + numOldRows);
    }

    /**
     * Helper method to clean up the main observable
     *
     * @param rockyRequest
     * @return
     */
    private Observable<ApiRockyRequestWrapper> getApiRockyRequest(RockyRequest rockyRequest) {
        final long rowId = rockyRequest.id();


        return Observable.zip(
                Observable.just(rockyRequest), // no sense in re-querying this
                toEnumMap(Address.Type.class, Address.TABLE,
                        Address.SELECT_BY_TYPE, rowId, Address.MAPPER),
                toEnumMap(ContactMethod.Type.class, ContactMethod.TABLE,
                        ContactMethod.SELECT_BY_TYPE, rowId, ContactMethod.MAPPER),
                toEnumMap(Name.Type.class, Name.TABLE,
                        Name.SELECT_BY_TYPE, rowId, Name.MAPPER),
                toEnumMap(VoterClassification.Type.class, VoterClassification.TABLE,
                        VoterClassification.SELECT_BY_TYPE, rowId, VoterClassification.MAPPER),
                toEnumMap(VoterId.Type.class, VoterId.TABLE,
                        VoterId.SELECT_BY_TYPE, rowId, VoterId.MAPPER),
                toEnumMap(AdditionalInfo.Type.class, AdditionalInfo.TABLE,
                        AdditionalInfo.SELECT_BY_TYPE, rowId, AdditionalInfo.MAPPER),
                this::zipRockyRequest);
    }

    /**
     * @param clazz enum class
     * @param rowId rockyrequest row id
     */
    public <K extends Enum<K>, Object> Observable<EnumMap<K, Object>> toEnumMap(
            Class<K> clazz, String table, String query, long rowId, Func1<Cursor, Object> mapper) {

        //TODO think about filtering null values
        return Observable.from(clazz.getEnumConstants())
                .flatMap(k -> db.createQuery(table, query, new String[]{String.valueOf(rowId), k.toString()})
                        .mapToOneOrDefault(mapper, null)
                        .flatMap(v -> Observable.just(new AbstractMap.SimpleEntry<>(k, v)))
                        .limit(1))
                .filter(entry -> null != entry.getValue())
                .toList()
                .flatMap(simpleEntries -> {
                    EnumMap<K, Object> map = new EnumMap<>(clazz);
                    for (AbstractMap.SimpleEntry<K, Object> entry : simpleEntries) {
                        map.put(entry.getKey(), entry.getValue());
                    }
                    return Observable.just(map);
                });
    }

    /**
     * Poor man's ORM
     * <p>
     * A helper method to construct the {@link ApiRockyRequest object}
     *
     * @param rockyRequest
     * @param addresses
     * @param contactMethods
     * @param names
     * @param classifications
     * @param voterIds
     * @param additionalInfo
     * @return {@link ApiRockyRequestWrapper} object
     */
    @SuppressWarnings({"Convert2streamapi", "ArraysAsListWithZeroOrOneArgument"})
    private ApiRockyRequestWrapper zipRockyRequest(RockyRequest rockyRequest,
                                                   EnumMap<Address.Type, Address> addresses,
                                                   EnumMap<ContactMethod.Type, ContactMethod> contactMethods,
                                                   EnumMap<Name.Type, Name> names,
                                                   EnumMap<VoterClassification.Type, VoterClassification> classifications,
                                                   EnumMap<VoterId.Type, VoterId> voterIds,
                                                   EnumMap<AdditionalInfo.Type, AdditionalInfo> additionalInfo) {

        /*
        * Get address info objects, this is a bit "mucky" because of the way I collect the data
        * there will still be an address object even if the user didn't check the box for
        * that type
        */
        ApiAddress apiRegAddress = ApiAddress.fromAddress(addresses.get(REGISTRATION_ADDRESS));
        ApiAddress apiMailAddress = rockyRequest.hasMailingAddress() ?
                ApiAddress.fromAddress(addresses.get(MAILING_ADDRESS)) : null;
        ApiAddress apiPrevAddress = rockyRequest.hasPreviousAddress() ?
                ApiAddress.fromAddress(addresses.get(PREVIOUS_ADDRESS)) : null;

        // Get name info objects
        ApiName apiName = ApiName.fromName(names.get(CURRENT_NAME));
        ApiName apiPrevName = rockyRequest.hasPreviousName() ?
                ApiName.fromName(names.get(PREVIOUS_NAME)) : null;

        // Get registrant contact info objects
        List<ApiContactMethod> apiContactMethods = new ArrayList<>();
        for (Entry<ContactMethod.Type, ContactMethod> entry : contactMethods.entrySet()) {
            if (ASSISTANT_PHONE != entry.getKey()) {
                apiContactMethods.add(ApiContactMethod.fromContactMethod(
                        entry.getValue(), rockyRequest.phoneType()));
            }
        }

        // Get voter classification objects
        List<ApiVoterClassification> apiClassifications = new ArrayList<>();
        for (Entry<VoterClassification.Type, VoterClassification> entry : classifications.entrySet()) {
            apiClassifications.add(ApiVoterClassification.fromVoterClassification(entry.getValue()));
        }

        // Get voter ID objects
        List<ApiVoterId> apiVoterIds = new ArrayList<>();
        for (Entry<VoterId.Type, VoterId> entry : voterIds.entrySet()) {
            apiVoterIds.add(ApiVoterId.fromVoterId(entry.getValue()));
        }

        // get additional info objects
        List<ApiAdditionalInfo> apiAdditionalInfo = new ArrayList<>();
        for (Entry<AdditionalInfo.Type, AdditionalInfo> entry : additionalInfo.entrySet()) {
            apiAdditionalInfo.add(ApiAdditionalInfo.fromAdditionalInfo(entry.getValue()));
        }

        ApiSignature apiSignature = ApiSignature.fromDb(rockyRequest);
        ApiGeoLocation apiGeoLocation = ApiGeoLocation.fromDb(rockyRequest);

        // build voter registration helper object
        List<ApiContactMethod> helperContactMethods = new ArrayList<>();
        for (Entry<ContactMethod.Type, ContactMethod> entry : contactMethods.entrySet()) {
            if (ASSISTANT_PHONE == entry.getKey()) {
                helperContactMethods.add(ApiContactMethod.fromContactMethod(
                        entry.getValue(), rockyRequest.phoneType()));
            }
        }

        ApiRegistrationHelper helper = !rockyRequest.hasAssistant() ? null :
                ApiRegistrationHelper.builder()
                        .address(ApiAddress.fromAddress(addresses.get(ASSISTANT_ADDRESS)))
                        .name(ApiName.fromName(names.get(ASSISTANT_NAME)))
                        .contactMethods(helperContactMethods)
                        .build();

        // build voter registration api object
        ApiVoterRegistration apiVoterRegistration = ApiVoterRegistration.fromDb(rockyRequest,
                apiMailAddress, apiPrevAddress, apiRegAddress, apiName, apiPrevName, apiClassifications,
                apiSignature, apiVoterIds, apiContactMethods, apiAdditionalInfo, helper);

        // build voter records request api object
        ApiVoterRecordsRequest apiVoterRecordsRequest = ApiVoterRecordsRequest.fromDb(rockyRequest,
                apiVoterRegistration);

        // build rocky request api object
        ApiRockyRequest apiRockyRequest = ApiRockyRequest.fromDb(rockyRequest,
                apiVoterRecordsRequest, apiGeoLocation);

        return ApiRockyRequestWrapper.builder().apiRockyRequest(apiRockyRequest).build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
