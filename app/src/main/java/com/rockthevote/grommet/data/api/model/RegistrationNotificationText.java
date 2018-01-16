package com.rockthevote.grommet.data.api.model;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

/**
 * Created by Mechanical Man on 1/15/18.
 */

@AutoValue
public abstract class RegistrationNotificationText {

    @Json(name = "en")
    public abstract String english();

    @Json(name = "es")
    public abstract String spanish();

    public static JsonAdapter<RegistrationNotificationText> jsonAdapter(Moshi moshi) {
        return new AutoValue_RegistrationNotificationText.MoshiJsonAdapter(moshi);
    }

    public static Builder builder() {
        return new AutoValue_RegistrationNotificationText.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder english(String english);

        public abstract Builder spanish(String spanish);

        public abstract RegistrationNotificationText build();
    }
}
