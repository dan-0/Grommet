package com.rockthevote.grommet.data.api.model;


import com.google.auto.value.AutoValue;
import com.rockthevote.grommet.data.api.Normalize;
import com.rockthevote.grommet.data.api.StringNormalizerFactory;
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
    abstract String streetName2();

    @Normalize
    abstract String subAddress();

    @Normalize
    abstract String subAddressType();

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

        abstract Builder streetName2(String value);

        abstract Builder subAddress(String value);

        abstract Builder subAddressType(String value);

        abstract Builder municipalJurisdiction(String value);

        abstract Builder county(String value);

        abstract Builder state(String value);

        abstract Builder zipCode(String value);

        abstract ApiAddress build();
    }

    public static final class ApiAddressMoshiAdapter extends JsonAdapter<ApiAddress> {
        private final JsonAdapter<String> stringAdapter;

        public ApiAddressMoshiAdapter(Moshi moshi) {
            this.stringAdapter = moshi.adapter(String.class);
        }

        @Override
        public ApiAddress fromJson(JsonReader reader) throws IOException {
            reader.beginObject();
            String streetName = null;
            String streetName2 = null;
            String subAddress = null;
            String subAddressType = null;
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
                        streetName = stringAdapter.fromJson(reader);
                        break;
                    }
                    case "subAddress": {
                        subAddress = stringAdapter.fromJson(reader);
                        break;
                    }
                    case "subAddressType": {
                        subAddressType = stringAdapter.fromJson(reader);
                        break;
                    }
                    case "municipalJurisdiction": {
                        municipalJurisdiction = stringAdapter.fromJson(reader);
                        break;
                    }
                    case "county": {
                        county = stringAdapter.fromJson(reader);
                        break;
                    }
                    case "state": {
                        state = stringAdapter.fromJson(reader);
                        break;
                    }
                    case "zip": {
                        zip = stringAdapter.fromJson(reader);
                        break;
                    }
                    default: {
                        reader.skipValue();
                    }
                }
            }
            reader.endObject();
            return new AutoValue_ApiAddress(streetName, streetName2, subAddress, subAddressType,
                    municipalJurisdiction, county, state, zip);
        }

        @Override
        public void toJson(JsonWriter writer, ApiAddress value) throws IOException {
            writer.beginObject();

            writer.name("numbered_thoroughfare_address");
            writer.beginObject();

            writer.name("complete_address_number");
            writer.nullValue();
            writer.name("complete_street_name");
            stringAdapter.toJson(writer,
                    StringNormalizerFactory.stripDiacritics(value.streetName()));

            writer.name("complete_sub_address");
            writer.beginArray();
            writer.beginObject();
            writer.name("sub_address_type");
            stringAdapter.toJson(writer,
                    StringNormalizerFactory.stripDiacritics(value.subAddressType()));
            writer.name("sub_address");
            stringAdapter.toJson(writer,
                    StringNormalizerFactory.stripDiacritics(value.subAddress()));
            writer.endObject();
            writer.beginObject();
            writer.name("sub_address_type");
            stringAdapter.toJson(writer, "LINE2");
            writer.name("sub_address");
            stringAdapter.toJson(writer,
                    StringNormalizerFactory.stripDiacritics(value.streetName2()));
            writer.endObject();
            writer.endArray();

            writer.name("complete_place_names");
            writer.beginArray();

            writer.beginObject();
            writer.name("place_name_type");
            writer.value("MunicipalJurisdiction");
            writer.name("place_name_value");
            stringAdapter.toJson(writer,
                    StringNormalizerFactory.stripDiacritics(value.municipalJurisdiction()));
            writer.endObject();

            writer.beginObject();
            writer.name("place_name_type");
            writer.value("County");
            writer.name("place_name_value");
            stringAdapter.toJson(writer, value.county());
            writer.endObject();

            writer.endArray();

            writer.name("state");
            stringAdapter.toJson(writer, value.state());
            writer.name("zip_code");
            stringAdapter.toJson(writer, value.zipCode());

            writer.endObject();
            writer.endObject();
        }
    }

}
