package com.rockthevote.grommet.data.api.model;

import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.rockthevote.grommet.data.api.Normalize;
import com.rockthevote.grommet.data.db.model.AdditionalInfo;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

@AutoValue
public abstract class ApiAdditionalInfo {

    abstract String name();

    @Normalize
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

    @Nullable
    public static ApiAdditionalInfo fromAdditionalInfo(AdditionalInfo additionalInfo) {
        if (null == additionalInfo) {
            return null;
        }
        return builder()
                .name(additionalInfo.type().toString())
                .stringValue(additionalInfo.stringValue())
                .build();
    }
}
