package com.rockthevote.grommet.data.db.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.rockthevote.grommet.data.db.Db;
import com.rockthevote.grommet.util.Dates;

import java.util.Date;
import java.util.Locale;

import rx.functions.Func1;

@AutoValue
@SuppressWarnings("mutable")
public abstract class RockyRequest implements Parcelable, BaseColumns {
    public static final String TABLE = "rocky_request";

    public static final String STATUS = "status";
    public static final String LANGUAGE = "lang";
    public static final String PHONE_TYPE = "phone_type";
    public static final String PARTNER_ID = "partner_id";
    public static final String OPT_IN_EMAIL = "opt_in_email";
    public static final String OPT_IN_SMS = "opt_in_sms";
    public static final String OPT_IN_VOLUNTEER = "opt_in_volunteer";
    public static final String PARTNER_OPT_IN_SMS = "partner_opt_in_sms";
    public static final String PARTNER_OPT_IN_EMAIL = "partner_opt_in_email";
    public static final String SOURCE_TRACKING_ID = "source_tracking_id";
    public static final String PARTNER_TRACKING_ID = "partner_tracking_id";
    public static final String OPEN_TRACKING_ID = "open_tracking_id";
    public static final String GENERATED_DATE = "generated_date";
    public static final String DATE_OF_BIRTH = "date_of_birth";
    public static final String HAS_MAILING_ADDRESS = "has_mailing_address";
    public static final String RACE = "race";
    public static final String PARTY = "party";
    public static final String SIGNATURE = "signature";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String HAS_PREVIOUS_NAME = "has_previous_name";
    public static final String HAS_PREVIOUS_ADDRESS = "has_previous_address";
    public static final String HAS_ASSISTANT = "has_assistant";
    public static final String OTHER_PARTY = "other_party";
    public static final String SESSION_ID = "session_id";

    public static final String SELECT_BY_ID = ""
            + "SELECT * FROM "
            + TABLE
            + " WHERE "
            + _ID + " = ? "
            + " LIMIT 1 ";

    public static final String SELECT_BY_STATUS = ""
            + "SELECT * FROM "
            + TABLE
            + " WHERE "
            + STATUS + " = ? ";

    public abstract long id();

    public abstract Status status();

    @Nullable
    public abstract String language();

    @Nullable
    public abstract PhoneType phoneType();

    public abstract String partnerId();

    public abstract boolean optInEmail();

    public abstract boolean optInSMS();

    public abstract boolean optInVolunteer();

    public abstract boolean partnerOptInSMS();

    public abstract boolean partnerOptInEmail();

    public abstract String sourceTrackingId();

    public abstract String partnerTrackingId();

    public abstract String openTrackingId();

    public abstract Date generatedDate();

    @Nullable
    public abstract Date dateOfBirth();

    public abstract boolean hasMailingAddress();

    public abstract Race race();

    public abstract Party party();

    @Nullable
    public abstract byte[] signature();

    public abstract long latitude();

    public abstract long longitude();

    public abstract boolean hasPreviousName();

    public abstract boolean hasPreviousAddress();

    public abstract boolean hasAssistant();

    @Nullable
    public abstract String otherParty();

    public abstract long sessionId();

    public static final Func1<Cursor, RockyRequest> MAPPER = cursor -> {
        long id = Db.getLong(cursor, _ID);
        Status status = Status.fromString(Db.getString(cursor, STATUS));
        String language = Db.getString(cursor, LANGUAGE);
        PhoneType phoneType = PhoneType.fromString(Db.getString(cursor, PHONE_TYPE));
        String partnerId = Db.getString(cursor, PARTNER_ID);
        boolean optInEmail = Db.getBoolean(cursor, OPT_IN_EMAIL);
        boolean optInSMS = Db.getBoolean(cursor, OPT_IN_SMS);
        boolean optInVolunteer = Db.getBoolean(cursor, OPT_IN_VOLUNTEER);
        boolean partnerOptInSMS = Db.getBoolean(cursor, PARTNER_OPT_IN_SMS);
        boolean partnerOptInEmail = Db.getBoolean(cursor, PARTNER_OPT_IN_EMAIL);
        String sourceTrackingId = Db.getString(cursor, SOURCE_TRACKING_ID);
        String partnerTrackingId = Db.getString(cursor, PARTNER_TRACKING_ID);
        String openTrackingId = Db.getString(cursor, OPEN_TRACKING_ID);
        Date generatedDate = Dates.parseISO8601_Date(Db.getString(cursor, GENERATED_DATE));
        Date dateOfBirth = Dates.parseISO8601_ShortDate(Db.getString(cursor, DATE_OF_BIRTH));
        boolean hasMailingAddress = Db.getBoolean(cursor, HAS_MAILING_ADDRESS);
        Race race = Race.fromString(Db.getString(cursor, RACE));
        Party party = Party.fromString(Db.getString(cursor, PARTY));
        byte[] signature = Db.getBlob(cursor, SIGNATURE);
        long latitude = Db.getLong(cursor, LATITUDE);
        long longitude = Db.getLong(cursor, LONGITUDE);
        boolean hasPreviousName = Db.getBoolean(cursor, HAS_PREVIOUS_NAME);
        boolean hasPreviousAddress = Db.getBoolean(cursor, HAS_PREVIOUS_ADDRESS);
        boolean hasAssistant = Db.getBoolean(cursor, HAS_ASSISTANT);
        String otherParty = Db.getString(cursor, OTHER_PARTY);
        long sessionId = Db.getLong(cursor, SESSION_ID);

        return new AutoValue_RockyRequest(id, status, language, phoneType, partnerId, optInEmail, optInSMS,
                optInVolunteer, partnerOptInSMS, partnerOptInEmail,
                sourceTrackingId, partnerTrackingId, openTrackingId,
                generatedDate, dateOfBirth, hasMailingAddress, race, party, signature,
                latitude, longitude, hasPreviousName, hasPreviousAddress, hasAssistant, otherParty,
                sessionId);
    };

    public static final class Builder {
        private final ContentValues values = new ContentValues();

        public Builder id(long id) {
            values.put(_ID, id);
            return this;
        }

        public Builder status(Status status) {
            values.put(STATUS, status.toString());
            return this;
        }

        public Builder language(String lang) {
            values.put(LANGUAGE, lang);
            return this;
        }

        public Builder phoneType(PhoneType phoneType) {
            values.put(PHONE_TYPE, phoneType.toString());
            return this;
        }

        public Builder partnerId(String id) {
            values.put(PARTNER_ID, id);
            return this;
        }

        public Builder optInEmail(boolean optIn) {
            values.put(OPT_IN_EMAIL, optIn);
            return this;
        }

        public Builder optInSMS(boolean optIn) {
            values.put(OPT_IN_SMS, optIn);
            return this;
        }

        public Builder optInVolunteer(boolean optIn) {
            values.put(OPT_IN_VOLUNTEER, optIn);
            return this;
        }

        public Builder partnerOptInSMS(boolean optIn) {
            values.put(PARTNER_OPT_IN_SMS, optIn);
            return this;
        }

        public Builder partnerOptInEmail(boolean optIn) {
            values.put(PARTNER_OPT_IN_EMAIL, optIn);
            return this;
        }

        public Builder sourceTrackingId(String id) {
            values.put(SOURCE_TRACKING_ID, id);
            return this;
        }

        public Builder partnerTrackingId(String id) {
            values.put(PARTNER_TRACKING_ID, id);
            return this;
        }

        public Builder openTrackingId(String id) {
            values.put(OPEN_TRACKING_ID, id);
            return this;
        }

        public Builder generateDate() {
            values.put(GENERATED_DATE, Dates.formatAsISO8601_Date(new Date()));
            return this;
        }

        public Builder dateOfBirth(Date date) {
            values.put(DATE_OF_BIRTH, Dates.formatAsISO8601_ShortDate(date));
            return this;
        }

        public Builder hasMailingAddress(boolean val) {
            values.put(HAS_MAILING_ADDRESS, val);
            return this;
        }

        public Builder race(Race race) {
            values.put(RACE, race.toString());
            return this;
        }

        public Builder party(Party party) {
            values.put(PARTY, party.toString());
            return this;
        }

        /**
         *
         * @param signature a Base64 encoded png string (NO_WRAP flag)
         * @return
         */
        public Builder signature(byte[] signature) {
            values.put(SIGNATURE, signature);
            return this;
        }

        public Builder latitude(long latitude) {
            values.put(LATITUDE, latitude);
            return this;
        }

        public Builder longitude(long longitude) {
            values.put(LONGITUDE, longitude);
            return this;
        }

        public Builder hasPreviousName(boolean hasPreviousName) {
            values.put(HAS_PREVIOUS_NAME, hasPreviousName);
            return this;
        }

        public Builder hasPreviousAddress(boolean hasPreviousAddress) {
            values.put(HAS_PREVIOUS_ADDRESS, hasPreviousAddress);
            return this;
        }

        public Builder hasAssistant(boolean hasAssistant) {
            values.put(HAS_ASSISTANT, hasAssistant);
            return this;
        }

        public Builder otherParty(String otherParty) {
            values.put(OTHER_PARTY, otherParty);
            return this;
        }

        public Builder sessionId(long sessionId) {
            values.put(SESSION_ID, sessionId);
            return this;
        }

        public ContentValues build() {
            return values;
        }
    }

    public enum Race {
        OTHER("Other", "OTRO"),
        AM_IND_AK_NATIVE("American Indian / Alaskan Native", "Nativoamericano"),
        ASIAN_PACIFIC_ISLANDER("Asian / Pacific Islander", "Asiatico"),
        BLACK("Black (not Hispanic)", "Afroamericano"),
        HISPANIC("Hispanic", "Hispano"),
        MULTI_RACIAL("Multi-Racial", "Multi"),
        WHITE("White (Not Hispanic)", "Anglosajón"),
        DECLINE("Decline to state", "Rechazar");

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
            return OTHER;
        }
    }

    public enum Party {
        DEMOCRATIC("Democratic", "Demócrata"),
        REPUBLICAN("Republican", "Republicano"),
        NO_PARTY("None", "Ninguno"),
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
}
