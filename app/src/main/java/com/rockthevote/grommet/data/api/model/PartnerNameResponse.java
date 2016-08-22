package com.rockthevote.grommet.data.api.model;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

@AutoValue
public abstract class PartnerNameResponse {
    @Json(name = "is_valid")
    abstract boolean isValid();

    @Json(name = "partner_name")
    abstract String partnerName();

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

        public abstract PartnerNameResponse build();
    }

}