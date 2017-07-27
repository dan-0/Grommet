package com.rockthevote.grommet.data.db.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.rockthevote.grommet.data.db.Db;
import com.rockthevote.grommet.util.Dates;

import java.util.Date;

import rx.functions.Func1;

/**
 * Created by Mechanical Man, LLC on 7/14/17. Grommet
 */

@AutoValue
public abstract class Session implements Parcelable, BaseColumns {

    public static final String TABLE = "session";


    public static final String SESSION_ID = "session_id";
    public static final String SESSION_STATUS = "session_status";
    public static final String CLOCK_IN_TIME = "clock_in_time";
    public static final String CLOCK_OUT_TIME = "clock_out_time";
    public static final String CLOCK_IN_REPORTED = "clock_in_reported";
    public static final String CLOCK_OUT_REPORTED = "clock_out_reported";
    public static final String CANVASSER_NAME = "canvasser_name";
    public static final String SOURCE_TRACKING_ID = "source_tracking_id";
    public static final String PARTNER_TRACKING_ID = "partner_tracking_id";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String SESSION_TIMEOUT = "session_timeout";
    public static final String TOTAL_REGISTRATIONS = "total_registrations";
    public static final String TOTAL_ABANDONED = "total_abandoned";
    public static final String TOTAL_EMAIL_OPT_IN = "total_email_opt_in";
    public static final String TOTAL_SMS_OPT_IN = "total_sms_opt_in";
    public static final String TOTAL_INCLUDE_DLN = "total_include_dln";
    public static final String TOTAL_INCLUDE_SSN = "total_include_ssn";

    public static final String SELECT_UNREPORTED_CLOCK_IN = ""
            + "SELECT * FROM "
            + TABLE
            + " WHERE "
            + CLOCK_IN_REPORTED + " = " + Db.BOOLEAN_FALSE
            + " AND "
            + CLOCK_IN_TIME + " IS NOT NULL "
            + " LIMIT 1";

    public static final String SELECT_UNREPORTED_CLOCK_OUT = ""
            + "SELECT * FROM "
            + TABLE
            + " WHERE "
            + CLOCK_OUT_REPORTED + " = " + Db.BOOLEAN_FALSE
            + " AND "
            + CLOCK_OUT_TIME + " IS NOT NULL "
            + " LIMIT 1";

    public static final String SELECT_CURRENT_SESSION = ""
            + "SELECT * FROM "
            + TABLE
            + " ORDER BY "
            + _ID + " DESC "
            + " LIMIT 1";

    public static final String DELETE_REPORTED_ROWS_WHERE_CLAUSE = ""
            + CLOCK_OUT_REPORTED + " = " + Db.BOOLEAN_TRUE
            + " AND "
            + CLOCK_IN_REPORTED + " = " + Db.BOOLEAN_TRUE;

    public abstract long id();

    public abstract String sessionId();

    public abstract SessionStatus sessionStatus();

    @Nullable
    public abstract Date clockInTime();

    @Nullable
    public abstract Date clockOutTime();

    public abstract boolean clockInReported();

    public abstract boolean clockOutReported();

    @Nullable
    public abstract String canvasserName();

    @Nullable
    public abstract String sourceTrackingId();

    @Nullable
    public abstract String partnerTrackingId();

    public abstract long latitude();

    public abstract long longitude();

    public abstract long sessionTimeout();

    public abstract int totalRegistrations();

    public abstract int totalAbandoned();

    public abstract int totalEmailOptIn();

    public abstract int totalSMSOptIn();

    public abstract int totalIncludeDLN();

    public abstract int totalIncludeSSN();

    public static final Func1<Cursor, Session> MAPPER = new Func1<Cursor, Session>() {
        @Override
        public Session call(Cursor cursor) {
            long id = Db.getLong(cursor, _ID);
            String sessionId = Db.getString(cursor, SESSION_ID);
            SessionStatus sessionStatus = SessionStatus.fromString(Db.getString(cursor, SESSION_STATUS));
            Date clockInTime = Dates.parseISO8601_Date(Db.getString(cursor, CLOCK_IN_TIME));
            Date clockOutTime = Dates.parseISO8601_Date(Db.getString(cursor, CLOCK_OUT_TIME));
            boolean clockInReported = Db.getBoolean(cursor, CLOCK_IN_REPORTED);
            boolean clockOutReported = Db.getBoolean(cursor, CLOCK_OUT_REPORTED);
            String canvasserName = Db.getString(cursor, CANVASSER_NAME);
            String sourceTrackingId = Db.getString(cursor, SOURCE_TRACKING_ID);
            String partnerTrackingId = Db.getString(cursor, PARTNER_TRACKING_ID);
            long latitude = Db.getLong(cursor, LATITUDE);
            long longitude = Db.getLong(cursor, LONGITUDE);
            long sessionTimeout = Db.getLong(cursor, SESSION_TIMEOUT);
            int totalRegistrations = Db.getInt(cursor, TOTAL_REGISTRATIONS);
            int totalAbandoned = Db.getInt(cursor, TOTAL_ABANDONED);
            int totalEmailOptIn = Db.getInt(cursor, TOTAL_EMAIL_OPT_IN);
            int totalSMSOptIn = Db.getInt(cursor, TOTAL_SMS_OPT_IN);
            int totalIncludeDLN = Db.getInt(cursor, TOTAL_INCLUDE_DLN);
            int totalIncludeSSN = Db.getInt(cursor, TOTAL_INCLUDE_SSN);

            return new AutoValue_Session(id, sessionId, sessionStatus, clockInTime, clockOutTime, clockInReported,
                    clockOutReported, canvasserName, sourceTrackingId, partnerTrackingId, latitude,
                    longitude, sessionTimeout, totalRegistrations, totalAbandoned, totalEmailOptIn,
                    totalSMSOptIn, totalIncludeDLN, totalIncludeSSN);
        }
    };


    public static final class Builder {
        private final ContentValues values = new ContentValues();

        public Builder id(long id) {
            values.put(_ID, id);
            return this;
        }

        public Builder sessionId(String sessionId) {
            values.put(SESSION_ID, sessionId);
            return this;
        }

        public Builder sessionStatus(SessionStatus sessionStatus) {
            values.put(SESSION_STATUS, sessionStatus.toString());
            return this;
        }

        public Builder clockInTime(Date date) {
            values.put(CLOCK_IN_TIME, Dates.formatAsISO8601_Date(date));
            return this;
        }

        public Builder clockOutTime(Date date) {
            values.put(CLOCK_OUT_TIME, Dates.formatAsISO8601_Date(date));
            return this;
        }

        public Builder clockInReported(boolean val) {
            values.put(CLOCK_IN_REPORTED, val);
            return this;
        }

        public Builder clockOutReported(boolean val) {
            values.put(CLOCK_OUT_REPORTED, val);
            return this;
        }

        public Builder canvasserName(String val) {
            values.put(CANVASSER_NAME, val);
            return this;
        }

        public Builder sourceTrackingId(String val) {
            values.put(SOURCE_TRACKING_ID, val);
            return this;
        }

        public Builder partnerTrackingId(String val) {
            values.put(PARTNER_TRACKING_ID, val);
            return this;
        }

        public Builder latitude(long val) {
            values.put(LATITUDE, val);
            return this;
        }

        public Builder longitude(long val) {
            values.put(LONGITUDE, val);
            return this;
        }

        public Builder sessionTimeout(long val) {
            values.put(SESSION_TIMEOUT, val);
            return this;
        }

        public Builder totalRegistrations(int val) {
            values.put(TOTAL_REGISTRATIONS, val);
            return this;
        }

        public Builder totalAbandond(int val) {
            values.put(TOTAL_ABANDONED, val);
            return this;
        }

        public Builder totalEmailOptIn(int val) {
            values.put(TOTAL_EMAIL_OPT_IN, val);
            return this;
        }

        public Builder totalSMSOptIn(int val) {
            values.put(TOTAL_SMS_OPT_IN, val);
            return this;
        }

        public Builder totalIncludeDLN(int val) {
            values.put(TOTAL_INCLUDE_DLN, val);
            return this;
        }

        public Builder totalIncludeSSN(int val) {
            values.put(TOTAL_INCLUDE_SSN, val);
            return this;
        }

        public ContentValues build() {
            return values;
        }
    }


    public enum SessionStatus {
        NEW_SESSION("new"),
        SESSION_CLEARED("session_cleared"),
        DETAILS_ENTERED("details_entered"),
        CLOCKED_IN("clocked_in"),
        CLOCKED_OUT("clocked_out"),
        TIMED_OUT("timed_out"); // basically the same as clocked_out but it displays an alert dialog

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
