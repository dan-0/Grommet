package com.rockthevote.grommet.data.api.model;

import com.google.auto.value.AutoValue;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.rockthevote.grommet.util.Dates;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

@AutoValue
public abstract class ApiVoterRecordsRequest {

    abstract String type();

    @Json(name = "generated_date")
    abstract String generatedDate();

    @Json(name = "voter_registration")
    public abstract ApiVoterRegistration voterRegistration();

    public static JsonAdapter<ApiVoterRecordsRequest> jsonAdapter(Moshi moshi) {
        return new AutoValue_ApiVoterRecordsRequest.MoshiJsonAdapter(moshi);
    }

    static Builder builder() {
        return new AutoValue_ApiVoterRecordsRequest.Builder()
                .type("registration");
    }

    @AutoValue.Builder
    abstract static class Builder {
        abstract Builder type(String value);

        abstract Builder generatedDate(String value);

        abstract Builder voterRegistration(ApiVoterRegistration value);

        abstract ApiVoterRecordsRequest build();
    }

    public static ApiVoterRecordsRequest fromDb(RockyRequest rockyRequest,
                                                ApiVoterRegistration apiVoterRegistration){
        return builder()
                .generatedDate(Dates.formatAsISO8601_Date(rockyRequest.generatedDate()))
                .voterRegistration(apiVoterRegistration)
                .build();
    }
}
