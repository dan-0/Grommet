package com.rockthevote.grommet.data.api.model;


import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.rockthevote.grommet.data.db.model.ContactMethod;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.ArrayList;
import java.util.List;

import static com.rockthevote.grommet.data.db.model.ContactMethod.Type.PHONE;
import static com.rockthevote.grommet.data.db.model.RockyRequest.PhoneType;

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


//        List<String> capabilities = new ArrayList<>();
//        // right now we only support phone (no fax)
//        if (contactMethod.type() == PHONE ||
//                contactMethod.type() == ContactMethod.Type.ASSISTANT_PHONE) {
//            capabilities.add(ContactMethod.Capability.VOICE.toString());
//            if(phoneType == PhoneType.MOBILE){
//                capabilities.add(ContactMethod.Capability.SMS.toString());
//            }
//        }
//
//        /*
//        special replacement for assistant_phone. We need to just use "phone" as the string value
//        for this enum but we cannot change the enum since both voter and helper phone numbers
//        are stored in the same table, hence we need different types to differentiate.
//         */
//
//        String type;
//        switch (contactMethod.type()) {
//            case ASSISTANT_PHONE:
//            case PHONE:
//                type = PHONE.toString();
//                break;
//            default:
//                type = contactMethod.type().toString();
//                break;
//        }


}
