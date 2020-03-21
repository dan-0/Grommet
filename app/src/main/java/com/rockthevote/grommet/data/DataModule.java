package com.rockthevote.grommet.data;

import android.app.Application;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.api.ApiModule;
import com.rockthevote.grommet.data.api.RockyAdapterFactory;
import com.rockthevote.grommet.data.api.StringNormalizerFactory;
import com.rockthevote.grommet.data.api.model.DateAdapter;
import com.rockthevote.grommet.data.api.model.PartnerVolunteerText;
import com.rockthevote.grommet.data.api.model.RegistrationNotificationText;
import com.rockthevote.grommet.data.db.DbModule;
import com.rockthevote.grommet.data.prefs.AppVersion;
import com.rockthevote.grommet.data.prefs.CanvasserName;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.data.prefs.CurrentSessionRowId;
import com.rockthevote.grommet.data.prefs.DeviceID;
import com.rockthevote.grommet.data.prefs.EventName;
import com.rockthevote.grommet.data.prefs.EventZip;
import com.rockthevote.grommet.data.prefs.PartnerId;
import com.rockthevote.grommet.data.prefs.PartnerName;
import com.rockthevote.grommet.data.prefs.PartnerTimeout;
import com.rockthevote.grommet.data.prefs.PartnerVolunteerTextPref;
import com.rockthevote.grommet.data.prefs.PartnerVolunteerTextPreferenceConverter;
import com.rockthevote.grommet.data.prefs.RegistrationDeadline;
import com.rockthevote.grommet.data.prefs.RegistrationDeadlinePreferenceConverter;
import com.rockthevote.grommet.data.prefs.RegistrationText;
import com.rockthevote.grommet.data.prefs.RegistrationTextPreferenceConverter;
import com.rockthevote.grommet.ui.MainActivity;
import com.squareup.moshi.Moshi;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;

import static android.content.Context.MODE_PRIVATE;
import static com.jakewharton.byteunits.DecimalByteUnit.MEGABYTES;


@Module(
        includes = {
                ApiModule.class,
                DbModule.class
        },
        injects = {
                MainActivity.class
        },
        complete = false,
        library = true
)
public final class DataModule {
    static final int DISK_CACHE_SIZE = (int) MEGABYTES.toBytes(50);

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(Application app) {
        return app.getSharedPreferences("grommet", MODE_PRIVATE);
    }

    @Provides
    @Singleton
    ReactiveLocationProvider provideReactiveLocationProvider(Application app){
        return new ReactiveLocationProvider(app);
    }

    @Provides
    @Singleton
    RxSharedPreferences provideRxSharedPreferences(SharedPreferences prefs) {
        return RxSharedPreferences.create(prefs);
    }

    @Provides
    @Singleton
    @RegistrationDeadline
    Preference<Date> provideRegistrationDeadline(RxSharedPreferences prefs, Application app) {
        return prefs.getObject(
                app.getResources().getString(R.string.pref_key_registration_deadline),
                Calendar.getInstance().getTime(),
                new RegistrationDeadlinePreferenceConverter());
    }

    @Provides
    @Singleton
    @AppVersion
    Preference<Integer> provideAppVersion(RxSharedPreferences prefs, Application app) {
        return prefs.getInteger(
                app.getResources().getString(R.string.pref_key_app_version), 0);
    }

    @Provides
    @Singleton
    @RegistrationText
    Preference<RegistrationNotificationText> provideRegistrationText(RxSharedPreferences prefs, Application app,
                                                                     Moshi moshi) {

        RegistrationNotificationText defaultValue = RegistrationNotificationText.builder()
                .english("")
                .spanish("")
                .build();

        return prefs.getObject(
                app.getResources().getString(R.string.pref_key_registration_text),
                defaultValue,
                new RegistrationTextPreferenceConverter(RegistrationNotificationText.jsonAdapter(moshi)));
    }

    @Provides
    @Singleton
    @PartnerId
    Preference<String> providePartnerId(RxSharedPreferences prefs, Application app) {
        return prefs.getString(app.getResources().getString(R.string.pref_key_partner_id));
    }

    @Provides
    @Singleton
    @PartnerTimeout
    Preference<Long> providePartnerTimeout(RxSharedPreferences prefs, Application app) {
        return prefs.getLong(app.getResources().getString(R.string.pref_key_partner_timeout));
    }

    @Provides
    @Singleton
    @DeviceID
    Preference<String> provideDeviceId(RxSharedPreferences prefs, Application app) {
        return prefs.getString(app.getResources().getString(R.string.pref_key_device_id));
    }

    @Provides
    @Singleton
    @PartnerName
    Preference<String> providePartnerName(RxSharedPreferences prefs, Application app) {
        return prefs.getString(app.getResources().getString(R.string.pref_key_partner_name));
    }

    @Provides
    @Singleton
    @CanvasserName
    Preference<String> provideCanvasserName(RxSharedPreferences prefs, Application app) {
        return prefs.getString(app.getResources().getString(R.string.pref_key_canvasser_name));
    }

    @Provides
    @Singleton
    @EventZip
    Preference<String> provideEventZip(RxSharedPreferences prefs, Application app) {
        return prefs.getString(app.getResources().getString(R.string.pref_key_event_zip_code));
    }

    @Provides
    @Singleton
    @EventName
    Preference<String> provideEventName(RxSharedPreferences prefs, Application app) {
        return prefs.getString(app.getResources().getString(R.string.pref_key_event_name));
    }

    @Provides
    @Singleton
    @CurrentRockyRequestId
    Preference<Long> provideCurrentRockyRequestId(RxSharedPreferences prefs) {
        return prefs.getLong("cur_rocky_request_id");
    }

    @Provides
    @Singleton
    @PartnerVolunteerTextPref
    Preference<PartnerVolunteerText> providePartnerVolunteerText(RxSharedPreferences prefs, Application app,
                                                                 Moshi moshi) {
        PartnerVolunteerText defaultValue = PartnerVolunteerText.builder()
                .english("")
                .spanish("")
                .build();

        return prefs.getObject(
                app.getResources().getString(R.string.pref_key_partner_volunteer_text),
                defaultValue,
                new PartnerVolunteerTextPreferenceConverter(PartnerVolunteerText.jsonAdapter(moshi)));
    }

    @Provides
    @Singleton
    @CurrentSessionRowId
    Preference<Long> provideCurrentSessionRowtId(RxSharedPreferences prefs) {
        return prefs.getLong("cur_session_row_id");
    }

    @Provides
    @Singleton
    Moshi provideMoshi() {
        return new Moshi.Builder()
                .add(new StringNormalizerFactory())
                .add(RockyAdapterFactory.create())
                .add(new DateAdapter())
                .build();
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Application app) {
        return createOkHttpClient(app).build();
    }

    @Provides
    @Singleton
    FirebaseAnalytics provideFirebase(Application app) {
        return FirebaseAnalytics.getInstance(app);
    }

    @Provides
    @Singleton
    HockeyAppHelper provideHockeyAppHelper() {
        return new HockeyAppHelper() {
            @Override
            public void checkForUpdates(AppCompatActivity activity) {
                // do nothing
            }

            @Override
            public void checkForCrashes(AppCompatActivity activity) {
                // do nothing
            }

            @Override
            public void unRegister() {
                // do nothing
            }
        };
    }

    static OkHttpClient.Builder createOkHttpClient(Application app) {
        // Install an HTTP cache in the application cache directory.
        File cacheDir = new File(app.getCacheDir(), "http");
        Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);

        return new OkHttpClient.Builder()
                .cache(cache);
    }
}
