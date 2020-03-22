package com.rockthevote.grommet.data.db.model;

import android.os.Parcelable;
import android.provider.BaseColumns;
import androidx.annotation.NonNull;

import java.util.Locale;


public abstract class RockyRequest implements Parcelable, BaseColumns {

    public enum Status {
        IN_PROGRESS("in_progress"),
        ABANDONED("abandoned"),
        FORM_COMPLETE("form_complete"),
        REGISTER_SUCCESS("register_success"),
        REGISTER_SERVER_FAILURE("register_server_failure"),
        REGISTER_CLIENT_FAILURE("register_client_failure");

        private final String status;

        Status(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return status;
        }

        public static Status fromString(String status) {
            for (Status val : values()) {
                if (val.toString().equals(status)) {
                    return val;
                }
            }
            return ABANDONED;
        }
    }

    public enum Language {
        ENGLISH("en"),
        SPANISH("es");

        private final String language;

        Language(String language) {
            this.language = language;
        }

        @Override
        public String toString() {
            return language;
        }

        public static Language fromString(String language) {
            for (Language val : values()) {
                if (val.toString().equals(language)) {
                    return val;
                }
            }
            return ENGLISH;
        }
    }
}
