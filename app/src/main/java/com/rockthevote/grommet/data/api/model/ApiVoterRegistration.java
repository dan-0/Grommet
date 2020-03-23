package com.rockthevote.grommet.data.api.model;


import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.List;

@AutoValue
public abstract class ApiVoterRegistration {

    @Json(name = "registration_helper")
    @Nullable
    abstract ApiRegistrationHelper registrationHelper();

    @Json(name = "date_of_birth")
    abstract String dateOfBirth();

    @Json(name = "mailing_address")
    @Nullable
    abstract ApiAddress mailingAddress();

    @Json(name = "previous_registration_address")
    @Nullable
    abstract ApiAddress previousRegistrationAddress();

    @Json(name = "registration_address")
    abstract ApiAddress registrationAddress();

    @Json(name = "registration_address_is_mailing_address")
    abstract boolean regIsMail();

    public abstract ApiName name();

    @Json(name = "previous_name")
    @Nullable
    abstract ApiName previousName();

    abstract String gender();

    abstract String race();

    abstract String party();

    @Json(name = "voter_classifications")
    abstract List<ApiVoterClassification> voterClassifications();

    abstract ApiSignature signature();

    @Json(name = "voter_ids")
    abstract List<ApiVoterId> voterIds();

    @Json(name = "contact_methods")
    abstract List<ApiContactMethod> contactMethods();

    @Json(name = "additional_info")
    abstract List<ApiAdditionalInfo> additionalInfo();


    public static JsonAdapter<ApiVoterRegistration> jsonAdapter(Moshi moshi) {
        return new AutoValue_ApiVoterRegistration.MoshiJsonAdapter(moshi);
    }

    static Builder builder() {
        return new AutoValue_ApiVoterRegistration.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {
        abstract Builder registrationHelper(ApiRegistrationHelper registrationHelper);

        abstract Builder dateOfBirth(String value);

        abstract Builder mailingAddress(ApiAddress value);

        abstract Builder previousRegistrationAddress(ApiAddress value);

        abstract Builder registrationAddress(ApiAddress value);

        abstract Builder regIsMail(boolean value);

        abstract Builder name(ApiName value);

        abstract Builder previousName(ApiName value);

        abstract Builder gender(String value);

        abstract Builder race(String value);

        abstract Builder party(String value);

        abstract Builder voterClassifications(List<ApiVoterClassification> values);

        abstract Builder signature(ApiSignature value);

        abstract Builder voterIds(List<ApiVoterId> values);

        abstract Builder contactMethods(List<ApiContactMethod> values);

        abstract Builder additionalInfo(List<ApiAdditionalInfo> values);

        abstract ApiVoterRegistration build();
    }


//        String party;
//        switch (rockyRequest.party()) {
//            case OTHER_PARTY:
//                party = rockyRequest.otherParty();
//                break;
//            default:
//                party = rockyRequest.party().toString();
//                break;
//        }



}
