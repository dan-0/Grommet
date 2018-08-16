package com.rockthevote.grommet;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;

import com.f2prateek.rx.preferences2.Preference;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.LumberYard;
import com.rockthevote.grommet.data.NetworkChangeReceiver;
import com.rockthevote.grommet.data.api.RegistrationService;
import com.rockthevote.grommet.data.db.model.Session;
import com.rockthevote.grommet.data.prefs.CurrentSessionRowId;
import com.rockthevote.grommet.data.prefs.PartnerTimeout;
import com.rockthevote.grommet.ui.ActivityHierarchyServer;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.Date;

import javax.inject.Inject;

import dagger.ObjectGraph;
import timber.log.Timber;

import static com.rockthevote.grommet.data.db.model.Session.SessionStatus.CLOCKED_IN;
import static com.rockthevote.grommet.data.db.model.Session.SessionStatus.TIMED_OUT;
import static timber.log.Timber.DebugTree;

public final class GrommetApp extends Application {
    private ObjectGraph objectGraph;

    @Inject ActivityHierarchyServer activityHierarchyServer;
    @Inject LumberYard lumberYard;
    @Inject @PartnerTimeout Preference<Long> partnerTimeoutPref;

    @Inject BriteDatabase db;
    @Inject @CurrentSessionRowId Preference<Long> currentSessionRowId;

    BroadcastReceiver br = new NetworkChangeReceiver();

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

        registerActivityLifecycleCallbacks(activityHierarchyServer);

        // check for session timeout
        Cursor cursor = db.query(Session.SELECT_CURRENT_SESSION);
        if (cursor.moveToNext()) {
            Session session = Session.MAPPER.call(cursor);
            Date in = session.clockInTime();
            long timeoutMilliseconds = partnerTimeoutPref.get() * 60000;

            if (null != in
                    && session.sessionStatus() == CLOCKED_IN
                    && timeoutMilliseconds > 0 // zero means no timeout was set
                    && (System.currentTimeMillis() - in.getTime()) > timeoutMilliseconds) {

                long clockOutTime = in.getTime() + session.sessionTimeout();

                Session.Builder builder = new Session.Builder()
                        .clockOutTime(new Date(clockOutTime))
                        .sessionStatus(TIMED_OUT)
                        .clockOutReported(false);

                db.update(Session.TABLE,
                        builder.build(),
                        Session._ID + " = ? ", String.valueOf(currentSessionRowId.get()));
            }
        }
        cursor.close();

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        this.registerReceiver(br, filter);

        // check the db for rows that need to be uploaded
        Intent regService = new Intent(this, RegistrationService.class);
        startService(regService);

    }

    @Override
    public Object getSystemService(@NonNull String name) {
        if (Injector.matchesService(name)) {
            return objectGraph;
        }
        return super.getSystemService(name);
    }
}
