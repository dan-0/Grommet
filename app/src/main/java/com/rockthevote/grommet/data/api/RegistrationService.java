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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import rx.Observable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.rockthevote.grommet.data.db.model.Address.Type.MAILING;
import static com.rockthevote.grommet.data.db.model.Address.Type.PREVIOUS;
import static com.rockthevote.grommet.data.db.model.Address.Type.REGISTRATION;
import static com.rockthevote.grommet.data.db.model.Name.Type.CURRENT_NAME;
import static com.rockthevote.grommet.data.db.model.Name.Type.PREVIOUS_NAME;
import static com.rockthevote.grommet.data.db.model.RockyRequest.GENERATED_DATE;
import static com.rockthevote.grommet.data.db.model.RockyRequest.STATUS;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.ABANDONED;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.FORM_COMPLETE;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.IN_PROGRESS;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.REGISTER_FAILURE;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.REGISTER_SUCCESS;

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

            UploadNotification.notify(getApplicationContext(), 0);

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
                .flatMap(apiRockyRequest -> rockyService.register(apiRockyRequest))
                .subscribeOn(Schedulers.io())
                .doOnCompleted(() -> {
                    if (refCount.incrementAndGet() == totalCount.get()) {
                        Timber.d("RegistrationService stopping: work complete");
                        UploadNotification.cancel(getApplicationContext());
                        stopSelf();
                    }
                })
                .subscribe(regResponse -> {
                            RockyRequest.Status status = regResponse.isError() ?
                                    REGISTER_FAILURE : REGISTER_SUCCESS;
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
                db.createQuery(Address.TABLE, Address.SELECT_BY_ROCKY_REQUEST_ID,
                        String.valueOf(rowId))
                        .mapToList(Address.MAPPER),
                db.createQuery(ContactMethod.TABLE, ContactMethod.SELECT_BY_ROCKY_REQUEST_ID,
                        String.valueOf(rowId))
                        .mapToList(ContactMethod.MAPPER),
                db.createQuery(Name.TABLE, Name.SELECT_BY_TYPE,
                        new String[]{String.valueOf(rowId), CURRENT_NAME.toString()})
                        .mapToOne(Name.MAPPER),
                db.createQuery(Name.TABLE, Name.SELECT_BY_TYPE,
                        new String[]{String.valueOf(rowId), PREVIOUS_NAME.toString()})
                        .mapToOne(Name.MAPPER),
                db.createQuery(VoterClassification.TABLE, VoterClassification.SELECT_BY_ROCKY_REQUEST_ID,
                        String.valueOf(rowId))
                        .mapToList(VoterClassification.MAPPER),
                db.createQuery(VoterId.TABLE, VoterId.SELECT_BY_ROCKY_REQUEST_ID,
                        String.valueOf(rowId))
                        .mapToList(VoterId.MAPPER),
                db.createQuery(AdditionalInfo.TABLE, AdditionalInfo.SELECT_BY_ROCKY_REQUEST_ID,
                        String.valueOf(rowId))
                        .mapToList(AdditionalInfo.MAPPER),
                this::zipRockyRequest);
    }

    /**
     * Poor man's ORM
     * <p>
     * A helper method to construct the {@link ApiRockyRequest object}
     *
     * @param rockyRequest
     * @param addresses
     * @param contactMethods
     * @param name
     * @param prevName
     * @param classifications
     * @param voterIds
     * @param additionalInfo
     * @return {@link ApiRockyRequestWrapper} object
     */
    private ApiRockyRequestWrapper zipRockyRequest(RockyRequest rockyRequest,
                                                   List<Address> addresses,
                                                   List<ContactMethod> contactMethods,
                                                   Name name,
                                                   Name prevName,
                                                   List<VoterClassification> classifications,
                                                   List<VoterId> voterIds,
                                                   List<AdditionalInfo> additionalInfo) {

        if (addresses.size() > 3) {
            throw new UnsupportedOperationException("registrant cannot have more than 3 addresses");
        }

        ApiAddress apiRegAddress = ApiAddress.fromDb(Observable.from(addresses)
                .filter(address -> address.type() == REGISTRATION).toBlocking().single());
        ApiAddress apiMailAddress = ApiAddress.fromDb(Observable.from(addresses)
                .filter(address -> address.type() == MAILING).toBlocking().single());
        ApiAddress apiPrevAddress = ApiAddress.fromDb(Observable.from(addresses)
                .filter(address -> address.type() == PREVIOUS).toBlocking().single());

        ApiName apiName = ApiName.fromDb(name);
        ApiName apiPrevName = ApiName.fromDb(prevName);

        List<ApiContactMethod> apiContactMethods = new ArrayList<>(contactMethods.size());
        for (ContactMethod contactMethod : contactMethods) {
            apiContactMethods.add(ApiContactMethod.fromDb(contactMethod, rockyRequest.phoneType()));
        }

        List<ApiVoterClassification> apiClassifications = new ArrayList<>(classifications.size());
        for (VoterClassification classification : classifications) {
            apiClassifications.add(ApiVoterClassification.fromDb(classification));
        }

        List<ApiVoterId> apiVoterIds = new ArrayList<>(voterIds.size());
        for (VoterId voterId : voterIds) {
            apiVoterIds.add(ApiVoterId.fromDb(voterId));
        }

        List<ApiAdditionalInfo> apiAdditionalInfo = new ArrayList<>(additionalInfo.size());
        for (AdditionalInfo addInfo : additionalInfo) {
            apiAdditionalInfo.add(ApiAdditionalInfo.fromDb(addInfo));
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
