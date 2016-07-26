package com.rockthevote.grommet.data.api;


import com.rockthevote.grommet.data.api.model.ApiRockyRequest;

import retrofit2.adapter.rxjava.Result;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

public interface RockyService {

    @POST("register")
    Observable<Result> register(@Body ApiRockyRequest rockyRequest);
}
