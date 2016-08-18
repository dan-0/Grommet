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
import com.rockthevote.grommet.data.api.model.ApiRockyRequest;
import com.rockthevote.grommet.data.api.model.ApiRockyRequestWrapper;
import com.rockthevote.grommet.data.api.model.ApiSignature;
import com.rockthevote.grommet.data.api.model.ApiVoterClassification;
import com.rockthevote.grommet.data.api.model.ApiVoterId;
import com.rockthevote.grommet.data.api.model.ApiVoterRecordsRequest;
import com.rockthevote.grommet.data.api.model.ApiVoterRegistration;
import com.rockthevote.grommet.data.db.model.AdditionalInfo;
import com.rockthevote.grommet.data.db.model.Address;
import com.rockthevote.grommet.data.db.model.ContactMethod;
import com.rockthevote.grommet.data.db.model.Name;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.rockthevote.grommet.data.db.model.VoterClassification;
import com.rockthevote.grommet.data.db.model.VoterId;
import com.rockthevote.grommet.ui.UploadNotification;
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

import static com.rockthevote.grommet.data.db.model.Address.Type.MAILING_ADDRESS;
import static com.rockthevote.grommet.data.db.model.Address.Type.PREVIOUS_ADDRESS;
import static com.rockthevote.grommet.data.db.model.Address.Type.REGISTRATION_ADDRESS;
import static com.rockthevote.grommet.data.db.model.RockyRequest.GENERATED_DATE;
import static com.rockthevote.grommet.data.db.model.RockyRequest.STATUS;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.ABANDONED;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.FORM_COMPLETE;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.IN_PROGRESS;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.REGISTER_FAILURE;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.REGISTER_SUCCESS;
import static java.util.Map.Entry;

public class RegistrationService extends Service {

    @SuppressWarnings("WeakerAccess")
    @Inject BriteDatabase db;

    @SuppressWarnings("WeakerAccess")
    @Inject RockyService rockyService;

    private final AtomicInteger refCount = new AtomicInteger(0);
    private final AtomicInteger totalCount = new AtomicInteger(0);

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

        // check to make sure we have internet
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (isConnected) {

            Cursor cursor = db.query(RockyRequest.SELECT_BY_STATUS, FORM_COMPLETE.toString());
            int rows = cursor.getCount();
            cursor.close();
            totalCount.set(rows);

            if (0 == rows) {
                Timber.d("RegistrationService stopping: no rows to upload");
                stopSelf();
                return;
            }

            db.createQuery(RockyRequest.TABLE,
                    RockyRequest.SELECT_BY_STATUS, FORM_COMPLETE.toString())
                    .mapToList(RockyRequest.MAPPER)
                    .flatMap(Observable::from)
                    .take(rows)
                    .subscribe(this::doWork);

        } else {
            Timber.d("RegistrationService stopping: no wifi");

            // if we don't have connectivity start a receiver to listen for connectivity change
            // and restart this service
            Context context = getApplicationContext();
            ComponentName receiver = new ComponentName(context, NetworkChangeReceiver.class);
            PackageManager pm = context.getPackageManager();

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);

            stopSelf();
        }
    }

    private void doWork(final RockyRequest rockyRequest) {
        if (null == rockyRequest) {
            return;
        }

        getApiRockyRequest(rockyRequest) // returns Observable<ApiRockyRequest>
                .flatMap(apiRockyRequestWrapper -> rockyService.register(apiRockyRequestWrapper))
                .subscribeOn(Schedulers.io())
                .doOnCompleted(() -> {
                    if (refCount.incrementAndGet() == totalCount.get()) {
                        Timber.d("RegistrationService stopping: work complete");
                        stopSelf();
                    }
                })
                .subscribe(regResponse -> {
                    RockyRequest.Status status =
                            !regResponse.isError() && regResponse.response().isSuccessful()
                                    ? REGISTER_SUCCESS : REGISTER_FAILURE;

                    UploadNotification.notify(getApplicationContext(), status);

                            db.update(RockyRequest.TABLE,
                                    new RockyRequest.Builder()
                                            .status(status)
                                            .build(),
                                    RockyRequest._ID + " = ? ", String.valueOf(rockyRequest.id()));
                        }
                );
    }

    /**
     * check for <p>
     * {@link RockyRequest.Status#ABANDONED},<p>
     * {@link RockyRequest.Status#REGISTER_FAILURE},<p>
     * {@link RockyRequest.Status#REGISTER_SUCCESS} <p>
     * as well as {@link RockyRequest.Status#IN_PROGRESS} that are more than one hour old
     * rows and delete them
     */
    @SuppressLint("BinaryOperationInTimber") // it's addition, not concatenation
    private void cleanup() {
        String completedRows = ""
                + STATUS + " IN ("
                + "'" + ABANDONED + "', "
                + "'" + REGISTER_FAILURE + "',"
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
    @SuppressWarnings("Convert2streamapi")
    private ApiRockyRequestWrapper zipRockyRequest(RockyRequest rockyRequest,
                                                   EnumMap<Address.Type, Address> addresses,
                                                   EnumMap<ContactMethod.Type, ContactMethod> contactMethods,
                                                   EnumMap<Name.Type, Name> names,
                                                   EnumMap<VoterClassification.Type, VoterClassification> classifications,
                                                   EnumMap<VoterId.Type, VoterId> voterIds,
                                                   EnumMap<AdditionalInfo.Type, AdditionalInfo> additionalInfo) {


        ApiAddress apiRegAddress = ApiAddress.fromDb(addresses.get(REGISTRATION_ADDRESS));
        ApiAddress apiMailAddress = rockyRequest.hasMailingAddress() ?
                ApiAddress.fromDb(addresses.get(MAILING_ADDRESS)) : null;
        ApiAddress apiPrevAddress = rockyRequest.hasPreviousAddress() ?
                ApiAddress.fromDb(addresses.get(PREVIOUS_ADDRESS)) : null;

        ApiName apiName = ApiName.fromDb(names.get(Name.Type.CURRENT_NAME));
        ApiName apiPrevName = rockyRequest.hasPreviousName() ?
                ApiName.fromDb(names.get(Name.Type.PREVIOUS_NAME)) : null;

        List<ApiContactMethod> apiContactMethods = new ArrayList<>(contactMethods.size());

        for (Entry<ContactMethod.Type, ContactMethod> entry : contactMethods.entrySet()) {
            apiContactMethods.add(ApiContactMethod.fromDb(entry.getValue(), rockyRequest.phoneType()));
        }

        List<ApiVoterClassification> apiClassifications = new ArrayList<>();
        for (Entry<VoterClassification.Type, VoterClassification> entry : classifications.entrySet()) {
            apiClassifications.add(ApiVoterClassification.fromDb(entry.getValue()));
        }

        List<ApiVoterId> apiVoterIds = new ArrayList<>();
        for (Entry<VoterId.Type, VoterId> entry : voterIds.entrySet()) {
            apiVoterIds.add(ApiVoterId.fromDb(entry.getValue()));
        }

        List<ApiAdditionalInfo> apiAdditionalInfo = new ArrayList<>();
        for (Entry<AdditionalInfo.Type, AdditionalInfo> entry : additionalInfo.entrySet()) {
            apiAdditionalInfo.add(ApiAdditionalInfo.fromDb(entry.getValue()));
        }

        ApiSignature apiSignature = ApiSignature.fromDb(rockyRequest);
        ApiGeoLocation apiGeoLocation = ApiGeoLocation.fromDb(rockyRequest);

        ApiVoterRegistration apiVoterRegistration = ApiVoterRegistration.fromDb(rockyRequest,
                apiMailAddress, apiPrevAddress, apiRegAddress, apiName, apiPrevName, apiClassifications,
                apiSignature, apiVoterIds, apiContactMethods, apiAdditionalInfo);

        ApiVoterRecordsRequest apiVoterRecordsRequest = ApiVoterRecordsRequest.fromDb(rockyRequest,
                apiVoterRegistration);

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
