package com.rockthevote.grommet.data.api.model;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

@AutoValue
public abstract class PartnerNameResponse {
    @Json(name = "is_valid")
    public abstract boolean isValid();

    @Json(name = "partner_name")
    public abstract String partnerName();

    @Json(name = "session_timeout_length")
    public abstract int sessionTimeoutLength();

    public static JsonAdapter<PartnerNameResponse> jsonAdapter(Moshi moshi) {
        return new AutoValue_PartnerNameResponse.MoshiJsonAdapter(moshi);
    }

    public static Builder builder() {
        return new AutoValue_PartnerNameResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder isValid(boolean isValid);

        public abstract Builder partnerName(String partnerName);

        public abstract Builder sessionTimeoutLength(int sessionTimeoutLength);

        public abstract PartnerNameResponse build();
    }

}
