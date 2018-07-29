package com.rockthevote.grommet.data.api.model;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

/**
 * Created by Mechanical Man on 7/29/18.
 */
@AutoValue
public abstract class PartnerVolunteerText {
    @Json(name = "en")
    public abstract String english();

    @Json(name = "es")
    public abstract String spanish();


    public static JsonAdapter<PartnerVolunteerText> jsonAdapter(Moshi moshi) {
        return new AutoValue_PartnerVolunteerText.MoshiJsonAdapter(moshi);
    }

    public static PartnerVolunteerText.Builder builder() {
        return new AutoValue_PartnerVolunteerText.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder english(String english);

        public abstract Builder spanish(String spanish);

        public abstract PartnerVolunteerText build();
    }
}
