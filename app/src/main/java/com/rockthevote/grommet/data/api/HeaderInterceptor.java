package com.rockthevote.grommet.data.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class HeaderInterceptor implements Interceptor{
    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request().newBuilder()
                .header("Content-Type", "application/json")
                .build();
        return chain.proceed(request);
    }
}
