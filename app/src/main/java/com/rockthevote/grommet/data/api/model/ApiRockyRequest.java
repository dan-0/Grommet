package com.rockthevote.grommet.data.api.model;

import com.google.auto.value.AutoValue;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;


@AutoValue
public abstract class ApiRockyRequest {

    /*
     * language the info was collected in (i.e. english or spanish) english is default
     */
    @Json(name = "lang")
    abstract String lang();

    @Json(name = "phone_type")
    abstract String phoneType();

    @Json(name = "partner_id")
    abstract String partnerId();

    @Json(name = "opt_in_email")
    abstract boolean optInEmail();

    @Json(name = "opt_in_sms")
    abstract boolean optInSms();

    @Json(name = "opt_in_volunteer")
    abstract boolean optInVolunteer();

    @Json(name = "partner_opt_in_email")
    abstract boolean partnerOptInEmail();

    @Json(name = "partner_opt_in_sms")
    abstract boolean partnerOptInSms();

    @Json(name = "partner_opt_in_volunteer")
    abstract boolean partnerOptInVolunteer();

    @Json(name = "finish_with_state")
    abstract boolean finishWithState();

    @Json(name = "created_via_api")
    abstract boolean createdViaApi();

    @Json(name = "source_tracking_id")
    abstract String sourceTrackingId();

    @Json(name = "partner_tracking_id")
    abstract String partnerTrackingId();

    @Json(name = "geo_location")
    abstract ApiGeoLocation geoLocation();

    @Json(name = "open_tracking_id")
    abstract String openTrackingId();

    @Json(name = "voter_records_request")
    abstract ApiVoterRecordsRequest voterRecordsRequest();

    public static JsonAdapter<ApiRockyRequest> jsonAdapter(Moshi moshi) {
        return new AutoValue_ApiRockyRequest.MoshiJsonAdapter(moshi);
    }

    static Builder builder() {
        return new AutoValue_ApiRockyRequest.Builder()
                .lang("en")
                .optInEmail(false)
                .optInSms(false)
                .optInVolunteer(false)
                .partnerOptInVolunteer(false)
                .finishWithState(true)
                .createdViaApi(true);

    }

    @AutoValue.Builder
    abstract static class Builder {
        abstract Builder lang(String value);

        abstract Builder phoneType(String value);

        abstract Builder partnerId(String value);

        abstract Builder optInEmail(boolean value);

        abstract Builder optInSms(boolean value);

        abstract Builder optInVolunteer(boolean value);

        abstract Builder partnerOptInEmail(boolean value);

        abstract Builder partnerOptInSms(boolean value);

        abstract Builder partnerOptInVolunteer(boolean value);

        abstract Builder finishWithState(boolean value);

        abstract Builder createdViaApi(boolean value);

        abstract Builder sourceTrackingId(String value);

        abstract Builder partnerTrackingId(String value);

        abstract Builder geoLocation(ApiGeoLocation value);

        abstract Builder openTrackingId(String value);

        abstract Builder voterRecordsRequest(ApiVoterRecordsRequest value);

        abstract ApiRockyRequest build();
    }

    public static ApiRockyRequest fromDb(RockyRequest rockyRequest,
                                         ApiVoterRecordsRequest voterRecordsRequest,
                                         ApiGeoLocation geoLocation) {

        return builder()
                .phoneType(rockyRequest.phoneType().toString())
                .partnerId(rockyRequest.partnerId())
                .partnerOptInEmail(rockyRequest.partnerOptInEmail())
                .partnerOptInSms(rockyRequest.partnerOptInSMS())
                .optInVolunteer(rockyRequest.optInVolunteer())
                .partnerOptInVolunteer(rockyRequest.partnerOptInVolunteer())
                .sourceTrackingId(rockyRequest.sourceTrackingId())
                .partnerTrackingId(rockyRequest.partnerTrackingId())
                .geoLocation(geoLocation)
                .openTrackingId(rockyRequest.openTrackingId())
                .voterRecordsRequest(voterRecordsRequest)
                .build();

    }
}
