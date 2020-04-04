package com.rockthevote.grommet.data.db.model;

import android.os.Parcelable;
import android.provider.BaseColumns;
import androidx.annotation.Nullable;

public abstract class VoterClassificationLegacy implements Parcelable, BaseColumns {

    public enum Type {
        EIGHTEEN("eighteen_on_election_day"),
        CITIZEN("united_states_citizen"),
        SEND_COPY_IN_MAIL("send_copy_in_mail"),
        DECLARATION_AGREE("agree_to_declaration"),
        POLITICAL_PARTY_CHANGE("political_party_change");

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
