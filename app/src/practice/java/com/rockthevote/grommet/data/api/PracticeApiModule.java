package com.rockthevote.grommet.data.api;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;

/**
 * Created by Mechanical Man, LLC on 8/1/17. Grommet
 */

@Module(
        complete = false,
        library = true,
        overrides = true
)
public final class PracticeApiModule {
    public static final HttpUrl STAGING_API_URL = HttpUrl.parse("https://practice.rocky.rockthevote.com/api/v3/");

    @Provides
    @Singleton
    HttpUrl provideBaseUrl() {
        return STAGING_API_URL;
    }

    @Provides
    @Singleton
    HttpLoggingInterceptor provideLoggingInterceptor() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> Timber.tag("OkHttp").v(message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return loggingInterceptor;
    }

    @Provides
    @Singleton
    @Named("Api")
    OkHttpClient provideApiClient(OkHttpClient client,
                                  HttpLoggingInterceptor loggingInterceptor) {
        return ApiModule.createApiClient(client)
                .addInterceptor(loggingInterceptor)
                .build();
    }
}
