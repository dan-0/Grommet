package com.rockthevote.grommet.ui;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.f2prateek.rx.preferences.Preference;
import com.rockthevote.grommet.R;
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

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.IN_PROGRESS;
import static com.rockthevote.grommet.data.db.model.Session.SessionStatus.CLOCKED_IN;

public final class MainActivity extends BaseActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.viewpager) ViewPager viewPager;
    @BindView(R.id.main_content) CoordinatorLayout coordinatorLayout;

    @Inject @PartnerId Preference<String> partnerIdPref;

    @Inject @CanvasserName Preference<String> canvasserNamePref;

    @Inject @EventName Preference<String> eventNamePref;

    @Inject @EventZip Preference<String> eventZipPref;

    @Inject @CurrentRockyRequestId Preference<Long> currentRockyRequestId;

    @Inject ViewContainer viewContainer;

    @Inject BriteDatabase db;

    @Inject ReactiveLocationProvider locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.activity_main, getContentView());
        ButterKnife.bind(this, view);
        setSupportActionBar(toolbar);
        requestGPSPermission();
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

        db.createQuery(Session.TABLE,
                Session.SELECT_CURRENT_SESSION)
                .mapToOne(Session.MAPPER)
                .first()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(session -> {
                    if (CLOCKED_IN == session.sessionStatus()) {
                        createNewVoterRecord(session);
                    } else {
                        dialog.show();
                    }
                });
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
