package com.rockthevote.grommet.data.db.model;

import android.os.Parcelable;
import android.provider.BaseColumns;
import androidx.annotation.Nullable;

/**
 * Created by Mechanical Man, LLC on 7/14/17. Grommet
 */

public abstract class Session implements Parcelable, BaseColumns {

    public enum SessionStatus {
        NEW_SESSION("new"),
        PARTNER_UPDATE("partner_update"),
        SESSION_CLEARED("session_cleared"),
        DETAILS_ENTERED("details_entered"),
        CLOCKED_IN("clocked_in"),
        CLOCKED_OUT("clocked_out");

        private final String type;

        SessionStatus(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }

        @Nullable
        public static SessionStatus fromString(String type) {
            for (SessionStatus val : values()) {
                if (val.toString().equals(type)) {
                    return val;
                }
            }
            return null;
        }
    }

}
