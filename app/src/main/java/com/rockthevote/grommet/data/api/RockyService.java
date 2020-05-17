package com.rockthevote.grommet.data.api;


import com.rockthevote.grommet.data.api.model.ClockInRequest;
import com.rockthevote.grommet.data.api.model.ClockOutRequest;
import com.rockthevote.grommet.data.api.model.PartnerNameResponse;
import com.rockthevote.grommet.data.api.model.RegistrationResponse;
import com.rockthevote.grommet.data.db.model.RockyRequest;

import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.adapter.rxjava.Result;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface RockyService {

    @POST("voterregistrationrequest")
    Result<RegistrationResponse> register(@Body RequestBody rockyRequestBody);

    @GET("partnerIdValidation")
    Observable<Result<PartnerNameResponse>> getPartnerName(@Query("partner_id") String partnerId);

    @POST("clockIn")
    Observable<Result<Void>> clockIn(@Body ClockInRequest clockInRequest);

    @POST("clockOut")
    Observable<Result<Void>> clockOut(@Body ClockOutRequest clockOutRequest);
}
