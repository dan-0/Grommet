package com.rockthevote.grommet.data.api.model;

import com.google.auto.value.AutoValue;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

@AutoValue
public abstract class ApiGeoLocation {
    @Json(name = "lat")
    abstract long latitude();

    @Json(name = "long")
    abstract long longitude();

    public static JsonAdapter<ApiGeoLocation> jsonAdapter(Moshi moshi) {
        return new AutoValue_ApiGeoLocation.MoshiJsonAdapter(moshi);
    }

    static Builder builder() {
        return new AutoValue_ApiGeoLocation.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {
        abstract Builder latitude(long value);

        abstract Builder longitude(long value);

        abstract ApiGeoLocation build();
    }

    public static ApiGeoLocation fromDb(RockyRequest rockyRequest) {
        return builder()
                .latitude(rockyRequest.latitude())
                .longitude(rockyRequest.longitude())
                .build();
    }
}
