package com.rockthevote.grommet.data.db.model;

import android.os.Parcelable;
import android.provider.BaseColumns;

import java.util.Locale;

public abstract class AdditionalInfo implements Parcelable, BaseColumns {


    public enum Type {
        LANGUAGE_PREF("preferred_language"),
        ASSISTANT_DECLARATION("assistant_declaration");

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
            return LANGUAGE_PREF;
        }
    }


}
