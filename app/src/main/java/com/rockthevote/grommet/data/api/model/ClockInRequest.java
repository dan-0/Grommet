package com.rockthevote.grommet.data.api.model;

/**
 * Created by Mechanical Man, LLC on 7/13/17. Grommet
 */

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

@AutoValue
public abstract class ClockInRequest {

    @Json(name = "source_tracking_id")
    public abstract String sourceTrackingId();

    @Json(name = "partner_tracking_id")
    public abstract String partnerTrackingId();

    @Json(name = "geo_location")
    public abstract ApiGeoLocation geoLocation();

    @Json(name = "open_tracking_id")
    public abstract String openTrackingId();

    @Json(name = "canvasser_name")
    public abstract String canvasserName();

    @Json(name = "clock_in_datetime")
    public abstract String clockInDatetime();

    @Json(name = "session_timeout_length")
    public abstract long sessionTimeoutLength();

    public static JsonAdapter<ClockInRequest> jsonAdapter(Moshi moshi) {
        return new AutoValue_ClockInRequest.MoshiJsonAdapter(moshi);
    }

    public static Builder builder() {
        return new AutoValue_ClockInRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder sourceTrackingId(String sourceTrackingId);

        public abstract Builder partnerTrackingId(String partnerTrackingId);

        public abstract Builder geoLocation(ApiGeoLocation geoLocation);

        public abstract Builder openTrackingId(String openTrackingId);

        public abstract Builder canvasserName(String canvasserName);

        public abstract Builder clockInDatetime(String clockInDatetime);

        public abstract Builder sessionTimeoutLength(long sessionTimeoutLength);

        public abstract ClockInRequest build();

    }

}
