package com.rockthevote.grommet.data.api.model;

import com.google.auto.value.AutoValue;
import com.rockthevote.grommet.data.api.Normalize;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

@AutoValue
public abstract class ApiVoterId {

    abstract String type();

    @Normalize
    @Json(name = "string_value")
    abstract String stringValue();

    @Json(name = "attest_no_such_id")
    abstract boolean attestNoSuchId();

    public static JsonAdapter<ApiVoterId> jsonAdapter(Moshi moshi) {
        return new AutoValue_ApiVoterId.MoshiJsonAdapter(moshi);
    }

    static Builder builder() {
        return new AutoValue_ApiVoterId.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {
        abstract Builder type(String value);

        abstract Builder stringValue(String value);

        abstract Builder attestNoSuchId(boolean value);

        abstract ApiVoterId build();
    }


}
