package com.rockthevote.grommet.data.api.model;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.List;

@AutoValue
public abstract class RegistrationResponse {

    @Json(name = "registration_success")
    abstract boolean registrationSuccess();

    @Json(name = "transaction_id")
    abstract int transactionId();

    @Json(name = "errors")
    abstract List<String> otherErrors();

    public static JsonAdapter<RegistrationResponse> jsonAdapter(Moshi moshi) {
        return new AutoValue_RegistrationResponse.MoshiJsonAdapter(moshi);
    }

    public static PartnerNameResponse.Builder builder() {
        return null;
    }
    
    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder registrationSuccess(boolean success);

        public abstract Builder transactionId(int transactionId);

        public abstract Builder otherErrors(List<String> errors);

        public abstract RegistrationResponse build();
    }
}
