package com.rockthevote.grommet.data.db.model;

import android.os.Parcelable;
import android.provider.BaseColumns;
import androidx.annotation.Nullable;

public abstract class VoterIdLegacy implements Parcelable, BaseColumns {
    public enum Type {
        DRIVERS_LICENSE("drivers_license"),
        SSN_LAST_FOUR("ssn4");

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
}
