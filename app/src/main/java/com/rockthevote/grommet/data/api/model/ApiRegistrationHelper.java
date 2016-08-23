package com.rockthevote.grommet.data.api.model;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.List;

@AutoValue
public abstract class ApiRegistrationHelper {

    @Json(name = "registration_helper_type")
    public abstract String type();

    public abstract ApiName name();

    public abstract ApiAddress address();

    @Json(name = "contact_methods")
    abstract List<ApiContactMethod> contactMethods();


    public static JsonAdapter<ApiRegistrationHelper> jsonAdapter(Moshi moshi) {
        return new AutoValue_ApiRegistrationHelper.MoshiJsonAdapter(moshi);
    }

    public static Builder builder() {
        return new AutoValue_ApiRegistrationHelper.Builder()
                .type("assistant");
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder type(String type);

        public abstract Builder name(ApiName name);

        public abstract Builder address(ApiAddress address);

        public abstract Builder contactMethods(List<ApiContactMethod> contactMethods);

        public abstract ApiRegistrationHelper build();
    }
}
