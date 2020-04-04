package com.rockthevote.grommet.data.db.model;

import android.os.Parcelable;
import android.provider.BaseColumns;
import androidx.annotation.NonNull;

public abstract class NameLegacy implements Parcelable, BaseColumns {

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

}
