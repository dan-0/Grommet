package com.rockthevote.grommet.data.api.model;


import com.google.auto.value.AutoValue;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.List;

@AutoValue
public abstract class ApiContactMethod {

    abstract String type();
    abstract String value();
    abstract List<String> capabilities();

    public static JsonAdapter<ApiContactMethod> jsonAdapter(Moshi moshi){
        return new AutoValue_ApiContactMethod.MoshiJsonAdapter(moshi);
    }

    static Builder builder(){
        return new AutoValue_ApiContactMethod.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder{
        abstract Builder type(String value);
        abstract Builder value(String value);
        abstract Builder capabilities(List<String> values);
        abstract ApiContactMethod build();
    }
}
