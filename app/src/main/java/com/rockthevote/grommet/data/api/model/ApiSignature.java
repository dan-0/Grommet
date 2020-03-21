package com.rockthevote.grommet.data.api.model;


import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

@AutoValue
public abstract class ApiSignature {

    @Json(name = "mime_type")
    abstract String mimeType();

    abstract String image();

    public static JsonAdapter<ApiSignature> jsonAdapter(Moshi moshi) {
        return new AutoValue_ApiSignature.MoshiJsonAdapter(moshi);
    }

    static Builder builder() {
        return new AutoValue_ApiSignature.Builder()
                .mimeType("image/png");
    }

    @AutoValue.Builder
    abstract static class Builder {
        abstract Builder mimeType(String value);

        /**
         * @param image base-64 encoded png (NO_WRAP flag)
         */
        abstract Builder image(String image);

        abstract ApiSignature build();
    }

}
