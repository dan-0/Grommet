package com.rockthevote.grommet.data.db.model;

import android.os.Parcelable;
import android.provider.BaseColumns;

public abstract class Address implements Parcelable, BaseColumns {

    public enum Type {
        MAILING_ADDRESS("mailing_address"),
        PREVIOUS_ADDRESS("previous_address"),
        REGISTRATION_ADDRESS("registration_address"),
        ASSISTANT_ADDRESS("assistant_address");

        private final String type;

        Type(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }

        public static Type fromString(String type) {
            for (Type val : values()) {
                if (val.toString().equals(type)) {
                    return val;
                }
            }
            return MAILING_ADDRESS;
        }
    }

}
