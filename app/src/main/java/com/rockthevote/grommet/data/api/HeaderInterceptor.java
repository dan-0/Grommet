package com.rockthevote.grommet.data.api;

import com.rockthevote.grommet.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class HeaderInterceptor implements Interceptor{
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        Request.Builder requestBuilder = original.newBuilder()
                .header("Grommet-Version", BuildConfig.VERSION_NAME);

        Request request = requestBuilder.build();
        return chain.proceed(request);
    }
}
