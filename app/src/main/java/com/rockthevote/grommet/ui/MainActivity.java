package com.rockthevote.grommet.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.f2prateek.rx.preferences.Preference;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.rockthevote.grommet.data.prefs.CanvasserName;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.data.prefs.EventName;
import com.rockthevote.grommet.data.prefs.EventZip;
import com.rockthevote.grommet.data.prefs.PartnerId;
import com.rockthevote.grommet.ui.registration.RegistrationActivity;
import com.rockthevote.grommet.ui.settings.SettingsActivity;
import com.rockthevote.grommet.util.Strings;
import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import static com.rockthevote.grommet.data.db.model.RockyRequest.SELECT_BY_STATUS;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.FORM_COMPLETE;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.IN_PROGRESS;

public final class MainActivity extends BaseActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.total_registered) TextView totalRegistered;
    @BindView(R.id.registered_today) TextView registeredToday;

    @Inject
    @PartnerId
    Preference<String> partnerIdPref;

    @Inject
    @CanvasserName
    Preference<String> canvasserNamePref;

    @Inject
    @EventName
    Preference<String> eventNamePref;

    @Inject
    @EventZip
    Preference<String> eventZipPref;

    @Inject
    @CurrentRockyRequestId
    Preference<Long> currentRockyRequestId;

    @Inject ViewContainer viewContainer;

    @Inject BriteDatabase db;

    private CompositeSubscription subscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = getLayoutInflater().inflate(R.layout.activity_main, getContentView());
        ButterKnife.bind(this, v);
    }

    @Override
    protected void onResume() {
        super.onResume();
        subscriptions = new CompositeSubscription();

        //TODO update this once we have the networking portion complete
        subscriptions.add(db.createQuery(RockyRequest.TABLE, SELECT_BY_STATUS, FORM_COMPLETE.toString())
                .mapToList(RockyRequest.MAPPER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rockyRequests -> {
                    totalRegistered.setText(String.valueOf(rockyRequests.size()));
                    registeredToday.setText(String.valueOf(rockyRequests.size()));
                }));
    }

    @Override
    protected void onPause() {
        super.onPause();
        subscriptions.unsubscribe();
    }

    @OnClick(R.id.button_new_voter)
    public void onClick(View v) {

        Observable.just(partnerIdPref.get(),
                canvasserNamePref.get(),
                eventNamePref.get(),
                eventZipPref.get())
                .all(s -> !Strings.isBlank(s))
                .subscribe(noneAreEmpty -> {
                    if (noneAreEmpty) {
                        createNewVoterRecord();
                        startActivity(new Intent(this, RegistrationActivity.class));
                    } else {
                        new AlertDialog.Builder(this)
                                .setMessage(R.string.incomplete_profile_alert)
                                .setNegativeButton(R.string.dialog_no_thanks, (dialogInterface, i) -> dialogInterface.dismiss())
                                .setPositiveButton(R.string.dialog_enter_info, (dialogInterface, i) -> {
                                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                                }).create().show();
                    }
                });
    }

    private void createNewVoterRecord() {
        long rockyRequestRowId = db.insert(RockyRequest.TABLE, new RockyRequest.Builder()
                .status(IN_PROGRESS)
                .partnerId(partnerIdPref.get())
                .partnerTrackingId(eventZipPref.get())
                .sourceTrackingId(canvasserNamePref.get())
                .openTrackingId(eventNamePref.get())
                .build());

        currentRockyRequestId.set(rockyRequestRowId);
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
