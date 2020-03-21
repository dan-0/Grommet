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

    public static Builder builder() {
        return new AutoValue_ApiGeoLocation.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder latitude(long value);

        public abstract Builder longitude(long value);

        public abstract ApiGeoLocation build();
    }

}
