package com.rockthevote.grommet.data.api.model;

import com.google.auto.value.AutoValue;
import com.rockthevote.grommet.data.db.model.VoterClassification;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

@AutoValue
public abstract class ApiVoterClassification {

    abstract String type();

    abstract boolean assertion();

    public static JsonAdapter<ApiVoterClassification> jsonAdapter(Moshi moshi) {
        return new AutoValue_ApiVoterClassification.MoshiJsonAdapter(moshi);
    }

    static Builder builder() {
        return new AutoValue_ApiVoterClassification.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {
        abstract Builder type(String value);

        abstract Builder assertion(boolean value);

        abstract ApiVoterClassification build();
    }

    public static ApiVoterClassification fromDb(VoterClassification classification) {
        return builder()
                .type(classification.type().toString())
                .assertion(classification.assertion())
                .build();
    }
}
