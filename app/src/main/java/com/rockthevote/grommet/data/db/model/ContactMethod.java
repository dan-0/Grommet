package com.rockthevote.grommet.data.db.model;

import android.os.Parcelable;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

public abstract class ContactMethod implements Parcelable, BaseColumns {

    public enum Type {
        PHONE("phone"),
        ASSISTANT_PHONE("assistant_phone"),
        EMAIL("email");

        private final String type;

        Type(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }

        @Nullable
        public static Type fromString(String type) {
            for (Type val : values()) {
                if (val.toString().equals(type)) {
                    return val;
                }
            }
            return null;
        }
    }

    public enum Capability {
        VOICE("voice"), FAX("fax"), SMS("sms");

        private final String capability;

        Capability(String capability) {
            this.capability = capability;
        }

        @Override
        public String toString() {
            return capability;
        }

        @Nullable
        public static Capability fromString(String capability) {
            for (Capability val : values()) {
                if (val.toString().equals(capability)) {
                    return val;
                }
            }
            return null;
        }
    }
}
