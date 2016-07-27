package com.rockthevote.grommet.data.api.model;

import com.google.auto.value.AutoValue;
import com.rockthevote.grommet.data.db.model.AdditionalInfo;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

@AutoValue
public abstract class ApiAdditionalInfo {

    abstract String name();

    @Json(name = "string_value")
    abstract String stringValue();

    public static JsonAdapter<ApiAdditionalInfo> jsonAdapter(Moshi moshi) {
        return new AutoValue_ApiAdditionalInfo.MoshiJsonAdapter(moshi);
    }

    static Builder builder() {
        return new AutoValue_ApiAdditionalInfo.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {
        abstract Builder name(String value);

        abstract Builder stringValue(String value);

        abstract ApiAdditionalInfo build();
    }

    public static ApiAdditionalInfo fromDb(AdditionalInfo additionalInfo) {
        return builder()
                .name(additionalInfo.type().toString())
                .stringValue(additionalInfo.stringValue())
                .build();
    }
}
