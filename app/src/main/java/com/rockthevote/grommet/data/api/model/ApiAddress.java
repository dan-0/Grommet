package com.rockthevote.grommet.data.api.model;


import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.rockthevote.grommet.data.api.Normalize;
import com.rockthevote.grommet.data.db.model.Address;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

@AutoValue
public abstract class ApiAddress {

    @Normalize
    abstract String streetName();

    @Normalize
    abstract String subAddress();

    @Normalize
    abstract String municipalJurisdiction();

    abstract String county();

    abstract String state();

    abstract String zipCode();

    public static JsonAdapter<ApiAddress> jsonAdapter(Moshi moshi) {
        return new ApiAddressMoshiAdapter(moshi);
    }

    static Builder builder() {
        return new AutoValue_ApiAddress.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {
        abstract Builder streetName(String value);

        abstract Builder subAddress(String value);

        abstract Builder municipalJurisdiction(String value);

        abstract Builder county(String value);

        abstract Builder state(String value);

        abstract Builder zipCode(String value);

        abstract ApiAddress build();
    }

    public static final class ApiAddressMoshiAdapter extends JsonAdapter<ApiAddress> {
        private final JsonAdapter<String> streetNameAdapter;
        private final JsonAdapter<String> subAddressAdapter;
        private final JsonAdapter<String> municipalJurisdictionAdapter;
        private final JsonAdapter<String> countyAdapter;
        private final JsonAdapter<String> stateAdapter;
        private final JsonAdapter<String> zipAdapter;

        public ApiAddressMoshiAdapter(Moshi moshi) {
            this.streetNameAdapter = moshi.adapter(String.class);
            this.subAddressAdapter = moshi.adapter(String.class);
            this.municipalJurisdictionAdapter = moshi.adapter(String.class);
            this.countyAdapter = moshi.adapter(String.class);
            this.stateAdapter = moshi.adapter(String.class);
            this.zipAdapter = moshi.adapter(String.class);
        }

        @Override
        public ApiAddress fromJson(JsonReader reader) throws IOException {
            reader.beginObject();
            String streetName = null;
            String subAddress = null;
            String municipalJurisdiction = null;
            String county = null;
            String state = null;
            String zip = null;
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (reader.peek() == JsonReader.Token.NULL) {
                    reader.skipValue();
                    continue;
                }
                switch (name) {
                    case "streetName": {
                        streetName = streetNameAdapter.fromJson(reader);
                        break;
                    }
                    case "subAddress": {
                        subAddress = subAddressAdapter.fromJson(reader);
                        break;
                    }
                    case "municipalJurisdiction": {
                        municipalJurisdiction = municipalJurisdictionAdapter.fromJson(reader);
                        break;
                    }
                    case "county": {
                        county = countyAdapter.fromJson(reader);
                        break;
                    }
                    case "state": {
                        state = stateAdapter.fromJson(reader);
                        break;
                    }
                    case "zip": {
                        zip = zipAdapter.fromJson(reader);
                        break;
                    }
                    default: {
                        reader.skipValue();
                    }
                }
            }
            reader.endObject();
            return new AutoValue_ApiAddress(streetName, subAddress, municipalJurisdiction, county, state, zip);
        }

        @Override
        public void toJson(JsonWriter writer, ApiAddress value) throws IOException {
            writer.beginObject();

            writer.name("numbered_thoroughfare_address");
            writer.beginObject();

            writer.name("complete_address_number");
            writer.nullValue();
            writer.name("complete_street_name");
            streetNameAdapter.toJson(writer, value.streetName());

            writer.name("complete_sub_address");
            writer.beginObject();
            writer.name("sub_address_type");
            writer.value("APT");
            writer.name("sub_address");
            subAddressAdapter.toJson(writer, value.subAddress());
            writer.endObject();

            writer.name("complete_place_names");
            writer.beginArray();

            writer.beginObject();
            writer.name("place_name_type");
            writer.value("MunicipalJurisdiction");
            writer.name("place_name_value");
            municipalJurisdictionAdapter.toJson(writer, value.municipalJurisdiction());
            writer.endObject();

            writer.beginObject();
            writer.name("place_name_type");
            writer.value("County");
            writer.name("place_name_value");
            countyAdapter.toJson(writer, value.county());
            writer.endObject();

            writer.endArray();

            writer.name("state");
            stateAdapter.toJson(writer, value.state());
            writer.name("zip_code");
            zipAdapter.toJson(writer, value.zipCode());

            writer.endObject();
            writer.endObject();
        }
    }

    @Nullable
    public static ApiAddress fromAddress(Address address) {
        if(null == address){
            return null;
        }

        return builder()
                .state(address.state())
                .municipalJurisdiction(address.municipalJurisdiction())
                .county(address.county())
                .streetName(address.streetName())
                .subAddress(address.subAddress())
                .zipCode(address.zip())
                .build();
    }
}
