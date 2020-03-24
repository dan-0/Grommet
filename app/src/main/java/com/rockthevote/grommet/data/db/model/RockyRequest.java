package com.rockthevote.grommet.data.db.model;

import android.os.Parcelable;
import android.provider.BaseColumns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;


public abstract class RockyRequest implements Parcelable, BaseColumns {

    public enum Race {
        DECLINE("DECLINE TO STATE", "RECHAZAR"),
        ASIAN("ASIAN", "ASIÁTICO"),
        BLACK("BLACK OR AFRICAN AMERICAN", "AFROAMERICANO"),
        HISPANIC_OR_LATINO("HISPANIC OR LATINO", "HISPÁNO/A O LATINO/A"),
        AM_IND_AK_NATIVE("NATIVE AMERICAN OR ALASKAN NATIVE", "NATIVO AMERICANO/A O NATIVO/A DE ALASKA"),
        NATIVE_HAWAIIAN("NATIVE HAWAIIAN OR OTHER PACIFIC ISLANDER","HAWAIANO/A O ISLEÑO/A DEL PACÍFICO"),
        OTHER("OTHER", "OTRA RAZA"),
        MULTI_RACIAL("TWO OR MORE RACES", "DOS O MÁS RAZAS"),
        WHITE("WHITE", "ANGLOSAJÓN");

        private final String enRace;
        private final String esRace;

        Race(String enRace, String esRace) {
            this.enRace = enRace;
            this.esRace = esRace;
        }

        @Override
        public String toString() {
            if ("es".equals(Locale.getDefault().getLanguage())) {
                return esRace;
            } else {
                // default to english
                return enRace;
            }
        }

        public static Race fromString(String race) {
            for (Race val : values()) {
                if (val.enRace.equals(race) ||
                        val.esRace.equals(race)) {
                    return val;
                }
            }
            return DECLINE;
        }
    }

    public enum Party {
        DEMOCRATIC("Democratic", "Demócrata"),
        REPUBLICAN("Republican", "Republicano"),
        GREEN("Green", "Verde"),
        LIBERTARIAN("Libertarian","Libertario"),
        NO_PARTY("None (No Affiliation)", "Ninguno (Sin afiliación)"),
        OTHER_PARTY("Other", "Otro");

        private final String enParty;
        private final String esParty;

        Party(String enParty, String esParty) {
            this.enParty = enParty;
            this.esParty = esParty;
        }

        @Override
        public String toString() {
            if ("es".equals(Locale.getDefault().getLanguage())) {
                return esParty;
            } else {
                // default to english
                return enParty;
            }
        }

        public static Party fromString(String party) {
            for (Party val : values()) {
                if (val.enParty.equals(party) ||
                        val.esParty.equals(party)) {
                    return val;
                }
            }
            return OTHER_PARTY;
        }
    }

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

    public enum PhoneType {
        MOBILE("Mobile", "Móvil"),
        HOME("Home", "Casa"),
        WORK("Work", "Trabajo");

        private final String enPhoneType;
        private final String esPhoneType;

        PhoneType(String enPhoneType, String esPhoneType) {
            this.enPhoneType = enPhoneType;
            this.esPhoneType = esPhoneType;
        }

        @Override
        public @NonNull String toString() {
            if ("es".equals(Locale.getDefault().getLanguage())) {
                return esPhoneType;
            } else {
                // default to english
                return enPhoneType;
            }
        }

        @NonNull
        public static PhoneType fromString(String phoneType) {
            for (PhoneType value : PhoneType.values()) {
                if (value.enPhoneType.equals(phoneType) ||
                        value.esPhoneType.equals(phoneType)) {
                    return value;
                }
            }
            //use mobile as default
            return MOBILE;
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
