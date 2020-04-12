package com.rockthevote.grommet;

import android.app.Application;
import androidx.annotation.NonNull;

import com.f2prateek.rx.preferences2.Preference;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.LumberYard;
import com.rockthevote.grommet.data.api.model.PartnerVolunteerText;
import com.rockthevote.grommet.data.api.model.RegistrationNotificationText;
import com.rockthevote.grommet.data.prefs.AppVersion;
import com.rockthevote.grommet.data.prefs.CanvasserName;
import com.rockthevote.grommet.data.prefs.CurrentSessionRowId;
import com.rockthevote.grommet.data.prefs.DeviceID;
import com.rockthevote.grommet.data.prefs.EventName;
import com.rockthevote.grommet.data.prefs.EventZip;
import com.rockthevote.grommet.data.prefs.PartnerId;
import com.rockthevote.grommet.data.prefs.PartnerName;
import com.rockthevote.grommet.data.prefs.PartnerTimeout;
import com.rockthevote.grommet.data.prefs.PartnerVolunteerTextPref;
import com.rockthevote.grommet.data.prefs.RegistrationDeadline;
import com.rockthevote.grommet.data.prefs.RegistrationText;
import com.rockthevote.grommet.ui.ActivityHierarchyServer;
import com.squareup.leakcanary.LeakCanary;

import java.util.Date;

import javax.inject.Inject;

import dagger.ObjectGraph;
import timber.log.Timber;

import static timber.log.Timber.DebugTree;

public final class GrommetApp extends Application {
    private ObjectGraph objectGraph;

    @Inject ActivityHierarchyServer activityHierarchyServer;
    @Inject LumberYard lumberYard;
    @Inject @PartnerTimeout Preference<Long> partnerTimeoutPref;
    @Inject @AppVersion Preference<Integer> appVersionPref;

    @Inject @CurrentSessionRowId Preference<Long> currentSessionRowId;

    // preferences
    @Inject @PartnerId Preference<String> partnerIdPref;
    @Inject @PartnerName Preference<String> partnerNamePref;
    @Inject @RegistrationDeadline Preference<Date> registrationDeadlinePref;
    @Inject @RegistrationText Preference<RegistrationNotificationText> registrationTextPref;
    @Inject @PartnerVolunteerTextPref Preference<PartnerVolunteerText> partnerVolunteerTextPref;

    @Inject @CanvasserName Preference<String> canvasserNamePref;
    @Inject @EventName Preference<String> eventNamePref;
    @Inject @EventZip Preference<String> eventZipPref;
    @Inject @DeviceID Preference<String> deviceIdPref;

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
        LeakCanary.install(this);


        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        } else {
            // TODO start analytics tracking
        }

        objectGraph = ObjectGraph.create(Modules.list(this));
        objectGraph.inject(this);

        lumberYard.cleanUp();
        Timber.plant(lumberYard.tree());

        //clear out old data if we're upgrading the app
        versionUpgradeCheck();

        registerActivityLifecycleCallbacks(activityHierarchyServer);

    }

    @Override
    public Object getSystemService(@NonNull String name) {
        if (Injector.matchesService(name)) {
            return objectGraph;
        }
        return super.getSystemService(name);
    }

    private void versionUpgradeCheck() {

        if (BuildConfig.VERSION_CODE != appVersionPref.get()) {
            // clear all info
            partnerIdPref.delete();
            partnerNamePref.delete();
            registrationTextPref.delete();
            partnerVolunteerTextPref.delete();
            registrationDeadlinePref.delete();

            canvasserNamePref.delete();
            eventNamePref.delete();
            eventZipPref.delete();
            deviceIdPref.delete();

            // update current version
            appVersionPref.set(BuildConfig.VERSION_CODE);
        }
    }
}
