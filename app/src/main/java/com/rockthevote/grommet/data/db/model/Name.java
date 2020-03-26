package com.rockthevote.grommet.data.db.model;

import android.os.Parcelable;
import android.provider.BaseColumns;
import androidx.annotation.NonNull;

public abstract class Name implements Parcelable, BaseColumns {

    public enum Type {
        CURRENT_NAME("current_name"),
        PREVIOUS_NAME("previous_name"),
        ASSISTANT_NAME("assistant_name");

        private final String type;

        Type(String type) {
            this.type = type;
        }

        @Override
        @NonNull
        public String toString() {
            return type;
        }

        public static Type fromString(String type) {
            for (Type val : values()) {
                if (val.toString().equals(type)) {
                    return val;
                }
            }
            return CURRENT_NAME;
        }
    }

    public enum Gender {
        MALE("male"), FEMALE("female");

        private final String gender;

        Gender(String gender) {
            this.gender = gender;
        }

        @Override
        @NonNull
        public String toString() {
            return gender;
        }

        @NonNull
        public static Gender fromString(String gender) {
            for (Gender value : Gender.values()) {
                if (value.gender.equals(gender)) {
                    return value;
                }
            }
            return MALE;
        }

        @NonNull
        public static Gender fromPrefix(Prefix prefix) {
            switch (prefix) {
                case MS:
                case MRS:
                case MISS:
                    return FEMALE;
                case MR:
                default:
                    return MALE;
            }
        }
    }

}
