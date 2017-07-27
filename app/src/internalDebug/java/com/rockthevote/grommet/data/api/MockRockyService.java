package com.rockthevote.grommet.data.api;

import android.content.SharedPreferences;

import com.rockthevote.grommet.data.api.model.ApiRockyRequestWrapper;
import com.rockthevote.grommet.data.api.model.ClockInRequest;
import com.rockthevote.grommet.data.api.model.ClockOutRequest;
import com.rockthevote.grommet.data.api.model.PartnerNameResponse;
import com.rockthevote.grommet.data.api.model.RegistrationResponse;
import com.rockthevote.grommet.util.EnumPreferences;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.adapter.rxjava.Result;
import retrofit2.http.Body;
import retrofit2.http.Query;
import retrofit2.mock.BehaviorDelegate;
import retrofit2.mock.Calls;
import retrofit2.mock.MockRetrofit;
import rx.Observable;

@Singleton
public final class MockRockyService implements RockyService {

    private final BehaviorDelegate<RockyService> delegate;
    private final SharedPreferences preferences;
    private final Map<Class<? extends Enum<?>>, Enum<?>> responses = new LinkedHashMap<>();

    @Inject
    MockRockyService(MockRetrofit mockRetrofit, SharedPreferences preferences) {
        this.delegate = mockRetrofit.create(RockyService.class);
        this.preferences = preferences;

        //TODO create mock responses
        loadResponse(MockRegistrationResponse.class, MockRegistrationResponse.SUCCESS);
        loadResponse(MockPartnerNameResponse.class, MockPartnerNameResponse.SUCCESS);
        loadResponse(MockClockInResponse.class, MockClockInResponse.SUCCESS);
        loadResponse(MockClockOutResponse.class, MockClockOutResponse.SUCCESS);
    }

    /**
     * Initializes the current response for {@code responseClass} from {@code SharedPreferences}, or
     * uses {@code defaultValue} if a response was not found.
     */
    private <T extends Enum<T>> void loadResponse(Class<T> responseClass, T defaultValue) {
        responses.put(responseClass, EnumPreferences.getEnumValue(preferences, responseClass, //
                responseClass.getCanonicalName(), defaultValue));
    }

    public <T extends Enum<T>> T getResponse(Class<T> responseClass) {
        return responseClass.cast(responses.get(responseClass));
    }

    public <T extends Enum<T>> void setResponse(Class<T> responseClass, T value) {
        responses.put(responseClass, value);
        EnumPreferences.saveEnumValue(preferences, responseClass.getCanonicalName(), value);
    }

    @Override
    public Observable<Result<RegistrationResponse>> register(@Body ApiRockyRequestWrapper rockyRequestWrapper) {
        RegistrationResponse response = getResponse(MockRegistrationResponse.class).response;

        return delegate.returning(Calls.response(response)).register(rockyRequestWrapper);
    }

    @Override
    public Observable<Result<PartnerNameResponse>> getPartnerName(@Query("partner_id") String partnerId) {
        PartnerNameResponse response = getResponse(MockPartnerNameResponse.class).response;
        return delegate.returning(Calls.response(response)).getPartnerName(partnerId);
    }

    @Override
    public Observable<Result<Void>> clockIn(@Body ClockInRequest clockInRequest) {
        MockClockInResponse response = getResponse(MockClockInResponse.class);
        switch (response) {
            case SUCCESS:
                return delegate.returning(Calls.response(null)).clockIn(clockInRequest);
            case FAILURE:
                return delegate.returning(Calls.failure(null)).clockIn(clockInRequest);
            default:
                return delegate.returning(Calls.response(null)).clockIn(clockInRequest);
        }
    }

    @Override
    public Observable<Result<Void>> clockOut(@Body ClockOutRequest clockOutRequest) {
        MockClockOutResponse response = getResponse(MockClockOutResponse.class);
        switch (response) {
            case SUCCESS:
                return delegate.returning(Calls.response(null)).clockOut(clockOutRequest);
            case FAILURE:
                return delegate.returning(Calls.failure(null)).clockOut(clockOutRequest);
            default:
                return delegate.returning(Calls.response(null)).clockOut(clockOutRequest);
        }
    }
}
