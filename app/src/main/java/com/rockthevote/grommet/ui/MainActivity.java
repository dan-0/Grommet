package com.rockthevote.grommet.ui;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.legacy.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.f2prateek.rx.preferences2.Preference;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.HockeyAppHelper;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.rockthevote.grommet.data.db.model.Session;
import com.rockthevote.grommet.data.prefs.CanvasserName;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.data.prefs.EventName;
import com.rockthevote.grommet.data.prefs.EventZip;
import com.rockthevote.grommet.data.prefs.PartnerId;
import com.rockthevote.grommet.ui.registration.RegistrationActivity;
import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.FORM_COMPLETE;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.IN_PROGRESS;
import static com.rockthevote.grommet.data.db.model.Session.SessionStatus.CLOCKED_IN;

public final class MainActivity extends BaseActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.viewpager) ViewPager viewPager;
    @BindView(R.id.main_content) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.pending_registrations) TextView pendingRegistrations;
    @BindView(R.id.upload) Button upploadButton;

    @Inject @PartnerId Preference<String> partnerIdPref;

    @Inject @CanvasserName Preference<String> canvasserNamePref;

    @Inject @EventName Preference<String> eventNamePref;

    @Inject @EventZip Preference<String> eventZipPref;

    @Inject @CurrentRockyRequestId Preference<Long> currentRockyRequestId;

    @Inject ViewContainer viewContainer;

    @Inject BriteDatabase db;

    @Inject ReactiveLocationProvider locationProvider;

    @Inject HockeyAppHelper hockeyAppHelper;

    private CompositeSubscription subscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.obtain(this).inject(this);

        View view = getLayoutInflater().inflate(R.layout.activity_main, getContentView());
        ButterKnife.bind(this, view);
        setSupportActionBar(toolbar);
        requestGPSPermission();
        hockeyAppHelper.checkForUpdates(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        hockeyAppHelper.checkForCrashes(this);

        subscriptions = new CompositeSubscription();
//
        // check for registrations to upload
        subscriptions.add(db.createQuery(RockyRequest.TABLE, RockyRequest.COUNT_BY_STATUS, FORM_COMPLETE.toString())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(query -> {
                    Cursor cursor = query.run();
                    try {
                        if (cursor.moveToNext()) {
                            int count = cursor.getInt((0));
                            this.pendingRegistrations.setText(String.valueOf(count));

                            upploadButton.setEnabled(count > 0);
                        }
                    } finally {
                        cursor.close();
                    }
                }));
    }

    @Override
    protected void onPause() {
        super.onPause();
        hockeyAppHelper.unRegister();
        subscriptions.unsubscribe();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hockeyAppHelper.unRegister();
    }

    private void requestGPSPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
        }
    }

    @OnClick(R.id.fab)
    public void onClick(View v) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.clock_in_alert_title)
                .setIcon(R.drawable.ic_timer_black_24dp)
                .setMessage(R.string.clock_in_alert_message)
                .setPositiveButton(R.string.action_ok, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .create();

        Cursor cursor = db.query(Session.SELECT_CURRENT_SESSION);
        int rows = cursor.getCount();
        if (rows == 0) {
            dialog.show();
        } else {
            cursor.moveToNext();
            Session session = Session.MAPPER.call(cursor);
            if(session.sessionStatus() == CLOCKED_IN){
                createNewVoterRecord(session);
            } else {
                dialog.show();
            }
        }
        cursor.close();
    }

    /**
     * starts the upload process if wifi is available, else notifies the user there is no wifi.
     * The button is enabled/disabled by the presense of pending applications
     *
     * @param v
     */
    @OnClick(R.id.upload)
    public void onClickUpload(View v) {


    }


    private void createNewVoterRecord(Session session) {
        locationProvider.getLastKnownLocation()
                .singleOrDefault(null)
                .subscribe(location -> {
                    RockyRequest.Builder builder = new RockyRequest.Builder()
                            .status(IN_PROGRESS)
                            .partnerId(partnerIdPref.get())
                            .partnerTrackingId(eventZipPref.get())
                            .sourceTrackingId(session.sourceTrackingId())
                            .openTrackingId(eventNamePref.get())
                            .partnerOptInSMS(true) // override database default so we don't have to perform a migration
                            .generateDate();

                    if (null != location) {
                        builder
                                .latitude((long) location.getLatitude())
                                .longitude((long) location.getLongitude());
                    }

                    long rockyRequestRowId = db.insert(RockyRequest.TABLE, builder.build());
                    currentRockyRequestId.set(rockyRequestRowId);
                    startActivity(new Intent(this, RegistrationActivity.class),
                            ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                });
    }

    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_home;
    }
}
