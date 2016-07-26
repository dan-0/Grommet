package com.rockthevote.grommet.data.api.model;


import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

@AutoValue
public abstract class ApiSignature {

    @Json(name = "mime_type")
    abstract String mimeType();

    abstract byte[] image();

    public static JsonAdapter<ApiSignature> jsonAdapter(Moshi moshi) {
        return new AutoValue_ApiSignature.MoshiJsonAdapter(moshi);
    }

    static Builder builder() {
        return new AutoValue_ApiSignature.Builder()
                .mimeType("image/jpeg");
    }

    @AutoValue.Builder
    abstract static class Builder {
        abstract Builder mimeType(String value);

        abstract Builder image(byte[] image);

        abstract ApiSignature build();
    }
}
