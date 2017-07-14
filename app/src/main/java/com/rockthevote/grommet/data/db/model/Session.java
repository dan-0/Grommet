package com.rockthevote.grommet.data.db.model;

import android.database.Cursor;
import android.os.Parcelable;
import android.provider.BaseColumns;

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
    public static final String CLOCK_IN_TIME = "clock_in_time";
    public static final String CLOCK_OUT_TIME = "clock_out_time";
    public static final String CLOCK_IN_REPORTED = "clock_in_reported";
    public static final String CLOCK_OUT_REPORTED = "clock_out_reported";
    public static final String CANVASSER_NAME = "canvasser_name";
    public static final String SOURCE_TRACKING_ID = "source_tracking_id";
    public static final String PARTNER_TRACKING_ID = "partner_tracking_id";
    public static final String LATITUTDE = "latitutde";
    public static final String LONGITUDE = "longitude";
    public static final String SESSION_TIMEOUT = "session_timeout";
    public static final String TOTAL_REGISTRATIONS = "total_registrations";
    public static final String TOTAL_ABANDONED = "total_abandoned";
    public static final String TOTAL_INCLUDE_EMAIL = "total_include_email";
    public static final String TOTAL_INCLUDE_PHONE = "total_include_phone";
    public static final String TOTAL_INCLUDE_DLN = "total_include_dln";
    public static final String TOTAL_INCLUDE_SSN = "total_include_ssn";


    public abstract long id();

    public abstract long sessionId();

    public abstract Date clockInTime();

    public abstract Date clockOutTime();

    public abstract boolean clockInReported();

    public abstract boolean clockOutReported();

    public abstract String canvasserName();

    public abstract String sourceTrackingId();

    public abstract String partnerTrackingId();

    public abstract long latitude();

    public abstract long longitude();

    public abstract long sessionTimeout();

    public abstract int totalRegistrations();

    public abstract int totalAbandoned();

    public abstract int totalIncludeEmail();

    public abstract int totalIncludePhone();

    public abstract int totalIncludeDLN();

    public abstract int totalIncludeSSN();

    public static final Func1<Cursor, Session> MAPPER = new Func1<Cursor, Session>() {
        @Override
        public Session call(Cursor cursor) {
            long id = Db.getLong(cursor, _ID);
            long sessionId = Db.getLong(cursor, SESSION_ID);
            Date clockInTime = Dates.parseISO8601_Date(Db.getString(cursor, CLOCK_IN_TIME));
            Date clockOutTime = Dates.parseISO8601_Date(Db.getString(cursor, CLOCK_OUT_TIME));
            boolean clockInReported = Db.getBoolean(cursor, CLOCK_IN_REPORTED);
            boolean clockOutReported = Db.getBoolean(cursor, CLOCK_OUT_REPORTED);
            String canvasserName = Db.getString(cursor, CANVASSER_NAME);
            String sourceTrackingId = Db.getString(cursor, SOURCE_TRACKING_ID);
            String partnerTrackingId = Db.getString(cursor, PARTNER_TRACKING_ID);
            long latitude = Db.getLong(cursor, LATITUTDE);
            long longitude = Db.getLong(cursor, LONGITUDE);
            long sessionTimeout = Db.getLong(cursor, SESSION_TIMEOUT);
            int totalRegistrations = Db.getInt(cursor, TOTAL_REGISTRATIONS);
            int totalAbandoned = Db.getInt(cursor, TOTAL_ABANDONED);
            int totalIncludeEmail = Db.getInt(cursor, TOTAL_INCLUDE_EMAIL);
            int totalIncludePhone = Db.getInt(cursor, TOTAL_INCLUDE_PHONE);
            int totalIncludeDLN = Db.getInt(cursor, TOTAL_INCLUDE_DLN);
            int totalIncludeSSN = Db.getInt(cursor, TOTAL_INCLUDE_SSN);

            return new AutoValue_Session(id, sessionId, clockInTime, clockOutTime, clockInReported,
                    clockOutReported, canvasserName, sourceTrackingId, partnerTrackingId, latitude,
                    longitude, sessionTimeout, totalRegistrations, totalAbandoned, totalIncludeEmail,
                    totalIncludePhone, totalIncludeDLN, totalIncludeSSN);
        }
    };

}
