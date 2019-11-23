package com.rockthevote.grommet.data.api.model;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

/**
 * this is just to get that root JSON object
 */
@AutoValue
public abstract class ApiRockyRequestWrapper {

    @Json(name = "rocky_request")
    public abstract ApiRockyRequest apiRockyRequest();

    public static JsonAdapter<ApiRockyRequestWrapper> jsonAdapter(Moshi moshi) {
        return new AutoValue_ApiRockyRequestWrapper.MoshiJsonAdapter(moshi);
    }

    public static Builder builder() {
        return new AutoValue_ApiRockyRequestWrapper.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder apiRockyRequest(ApiRockyRequest request);

        public abstract ApiRockyRequestWrapper build();
    }
}
