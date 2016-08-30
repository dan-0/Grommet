package com.rockthevote.grommet.ui;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.f2prateek.rx.preferences.Preference;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.rockthevote.grommet.data.prefs.AppRegTotal;
import com.rockthevote.grommet.data.prefs.CanvasserName;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.data.prefs.EventName;
import com.rockthevote.grommet.data.prefs.EventRegTotal;
import com.rockthevote.grommet.data.prefs.EventZip;
import com.rockthevote.grommet.data.prefs.PartnerId;
import com.rockthevote.grommet.ui.registration.RegistrationActivity;
import com.rockthevote.grommet.ui.views.EditableActionView;
import com.rockthevote.grommet.ui.views.EventDetails;
import com.rockthevote.grommet.util.Strings;
import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.IN_PROGRESS;

public final class MainActivity extends BaseActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.event_details_toolbar) Toolbar eventToolbar;
    @BindView(R.id.total_registered) TextView totalRegistered;
    @BindView(R.id.registered_for_event) TextView registeredEvent;
    @BindView(R.id.event_details) EventDetails eventDetails;
    @BindView(R.id.editable_action_view) EditableActionView editableActionView;

    @Inject @PartnerId Preference<String> partnerIdPref;

    @Inject @CanvasserName Preference<String> canvasserNamePref;

    @Inject @EventName Preference<String> eventNamePref;

    @Inject @EventZip Preference<String> eventZipPref;

    @Inject @CurrentRockyRequestId Preference<Long> currentRockyRequestId;

    @Inject @EventRegTotal Preference<Integer> eventRegTotal;

    @Inject @AppRegTotal Preference<Integer> appRegtotal;

    @Inject ViewContainer viewContainer;

    @Inject BriteDatabase db;

    @Inject ReactiveLocationProvider locationProvider;

    private CompositeSubscription subscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = getLayoutInflater().inflate(R.layout.activity_main, getContentView());
        ButterKnife.bind(this, v);
        setSupportActionBar(eventToolbar);
        requestGPSPermission();
        eventDetails.setEditableActionView(editableActionView);
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

    @Override
    protected void onResume() {
        super.onResume();
        subscriptions = new CompositeSubscription();

        subscriptions.add(eventRegTotal.asObservable()
                .subscribe(eventTotal -> registeredEvent.setText(String.valueOf(eventTotal))));

        subscriptions.add(appRegtotal.asObservable()
                .subscribe(appTotal -> totalRegistered.setText(String.valueOf(appTotal))));
    }

    @Override
    protected void onPause() {
        super.onPause();
        subscriptions.unsubscribe();
    }

    @OnClick(R.id.fab)
    public void onClick(View v) {

        Observable.just(partnerIdPref.get(),
                canvasserNamePref.get(),
                eventNamePref.get(),
                eventZipPref.get())
                .all(s -> !Strings.isBlank(s))
                .subscribe(noneAreEmpty -> {
                    if (noneAreEmpty) {
                        createNewVoterRecord();
                    } else {
                        new AlertDialog.Builder(this)
                                .setMessage(R.string.incomplete_profile_alert)
                                .setNegativeButton(R.string.dialog_no_thanks, (dialogInterface, i) -> dialogInterface.dismiss())
                                .setPositiveButton(R.string.dialog_enter_info, (dialogInterface, i) -> {
                                    eventDetails.enableEditMode(true);
                                }).create().show();
                    }
                });
    }

    private void createNewVoterRecord() {
        locationProvider.getLastKnownLocation()
                .singleOrDefault(null)
                .subscribe(location -> {
                    RockyRequest.Builder builder = new RockyRequest.Builder()
                            .status(IN_PROGRESS)
                            .partnerId(partnerIdPref.get())
                            .partnerTrackingId(eventZipPref.get())
                            .sourceTrackingId(canvasserNamePref.get())
                            .openTrackingId(eventNamePref.get())
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
