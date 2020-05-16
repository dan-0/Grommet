package com.rockthevote.grommet.data.api

import com.rockthevote.grommet.data.api.model.ClockInRequest
import com.rockthevote.grommet.data.api.model.ClockOutRequest
import com.rockthevote.grommet.data.api.model.PartnerNameResponse
import com.rockthevote.grommet.data.api.model.RegistrationResponse
import com.rockthevote.grommet.data.db.model.RockyRequest
import retrofit2.Response
import retrofit2.adapter.rxjava.Result
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import rx.Observable

interface RockyService {
    @POST("voterregistrationrequest")
    fun register(@Body rockyRequestWrapper: RockyRequest?): Observable<Result<RegistrationResponse?>?>?

    @GET("partnerIdValidation")
    suspend fun getPartnerName(@Query("partner_id") partnerId: String?): Response<PartnerNameResponse?>

    @POST("clockIn")
    fun clockIn(@Body clockInRequest: ClockInRequest?): Observable<Result<Void?>?>?

    @POST("clockOut")
    fun clockOut(@Body clockOutRequest: ClockOutRequest?): Observable<Result<Void?>?>?
}