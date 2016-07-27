package com.rockthevote.grommet.data;

import android.app.Application;
import android.content.SharedPreferences;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.api.ApiModule;
import com.rockthevote.grommet.data.api.RegistrationService;
import com.rockthevote.grommet.data.api.RockyAdapterFactory;
import com.rockthevote.grommet.data.db.DbModule;
import com.rockthevote.grommet.data.prefs.AppRegTotal;
import com.rockthevote.grommet.data.prefs.CanvasserName;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.data.prefs.EventName;
import com.rockthevote.grommet.data.prefs.EventRegTotal;
import com.rockthevote.grommet.data.prefs.EventZip;
import com.rockthevote.grommet.data.prefs.PartnerId;
import com.squareup.moshi.Moshi;
import com.squareup.picasso.Picasso;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;
import static com.jakewharton.byteunits.DecimalByteUnit.MEGABYTES;


@Module(
        includes = {
                ApiModule.class,
                DbModule.class
        },
        injects = {
                RegistrationService.class
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
    @EventRegTotal
    Preference<Integer> provideEventRegTotal(RxSharedPreferences prefs, Application app){
        return prefs.getInteger(app.getResources().getString(R.string.pref_key_event_reg_total), 0);
    }

    @Provides
    @Singleton
    @AppRegTotal
    Preference<Integer> provideAppRegTotal(RxSharedPreferences prefs, Application app){
        return prefs.getInteger(app.getResources().getString(R.string.pref_key_app_reg_total), 0);
    }

    @Provides
    @Singleton
    @PartnerId
    Preference<String> providePartnerId(RxSharedPreferences prefs, Application app) {
        return prefs.getString(app.getResources().getString(R.string.pref_key_partner_id));
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
    Moshi provideMoshi() {
        return new Moshi.Builder()
                .add(RockyAdapterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Application app) {
        return createOkHttpClient(app).build();
    }

    @Provides
    @Singleton
    Picasso providePicasso(Application app, OkHttpClient client) {
        return new Picasso.Builder(app)
                .downloader(new OkHttp3Downloader(client))
                .listener((picasso, uri, e) -> Timber.e(e, "Failed to load image: %s", uri))
                .build();
    }

    static OkHttpClient.Builder createOkHttpClient(Application app) {
        // Install an HTTP cache in the application cache directory.
        File cacheDir = new File(app.getCacheDir(), "http");
        Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);

        return new OkHttpClient.Builder()
                .cache(cache);
    }
}
