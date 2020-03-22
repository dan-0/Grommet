package com.rockthevote.grommet.data.db.model;

import android.os.Parcelable;
import android.provider.BaseColumns;
import androidx.annotation.NonNull;

import java.util.Locale;

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

    public enum Suffix {
        EMPTY(""), JR("Jr"), SR("Sr"), SECOND("II"), THIRD("III"), FOURTH("IV"),
        FIFTH("V"), SIXTH("VI"), SEVENTH("VII");

        private final String suffix;

        Suffix(String suffix) {
            this.suffix = suffix;
        }

        @Override
        @NonNull
        public String toString() {
            return suffix;
        }

        @NonNull
        public static Suffix fromString(String suffix) {
            for (Suffix value : Suffix.values()) {
                if (value.suffix.equals(suffix)) {
                    return value;
                }
            }
            return EMPTY;
        }

    }

    public enum Prefix {
        MR("Mr", "Sr"),
        MS("Ms", "Ms"), // intentionally the same
        MRS("Mrs", "Srta"),
        MISS("Miss", "Sra");

        private final String enTitle;
        private final String esTitle;

        Prefix(String enTitle, String esTitle) {
            this.enTitle = enTitle;
            this.esTitle = esTitle;
        }

        @Override
        @NonNull
        public String toString() {
            if ("es".equals(Locale.getDefault().getLanguage())) {
                return esTitle;
            } else {
                // default to english
                return enTitle;
            }
        }

        @NonNull
        public static Prefix fromString(String title) {
            for (Prefix value : Prefix.values()) {
                if (value.enTitle.equals(title) ||
                        value.esTitle.equals(title)) {
                    return value;
                }
            }
            return MR;
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
