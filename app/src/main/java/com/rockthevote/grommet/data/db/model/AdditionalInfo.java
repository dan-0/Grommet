package com.rockthevote.grommet.data.db.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcelable;
import android.provider.BaseColumns;

import com.google.auto.value.AutoValue;
import com.rockthevote.grommet.data.db.Db;
import com.squareup.sqlbrite.BriteDatabase;

import rx.functions.Func1;

@AutoValue
public abstract class AdditionalInfo implements Parcelable, BaseColumns {
    public static final String TABLE = "additional_info";

    public static final String TYPE = "type";
    public static final String STRING_VALUE = "string_value";
    public static final String ROCKY_REQUEST_ID = "rocky_request_id";

    private static final String SELECT_BY_TYPE = ""
            + "SELECT * FROM "
            + TABLE
            + " WHERE "
            + ROCKY_REQUEST_ID + " = ? "
            + " AND "
            + TYPE + " = ? "
            + " LIMIT 1";

    public abstract long id();

    public abstract long rockyRequestId();

    public abstract Type type();

    public abstract String stringValue();

    public transient static final Func1<Cursor, AdditionalInfo> MAPPER = new Func1<Cursor, AdditionalInfo>() {
        @Override
        public AdditionalInfo call(Cursor cursor) {
            long id = Db.getLong(cursor, _ID);
            long rockyRequestId = Db.getLong(cursor, ROCKY_REQUEST_ID);
            Type type = Type.fromString(Db.getString(cursor, TYPE));
            String stringValue = Db.getString(cursor, STRING_VALUE);
            return new AutoValue_AdditionalInfo(id, rockyRequestId, type, stringValue);
        }
    };

    public static void insertOrUpdate(BriteDatabase db, long rockyRequestRowId,
                                      Type type, ContentValues values) {

        values.put(ROCKY_REQUEST_ID, rockyRequestRowId);
        values.put(TYPE, type.toString());

        Cursor cursor = db.query(
                SELECT_BY_TYPE,
                String.valueOf(rockyRequestRowId),
                type.toString());

        if (cursor.moveToNext()) {
            long rowId = Db.getLong(cursor, _ID);
            db.update(TABLE, values, _ID + " = ? ", String.valueOf(rowId));
        } else {
            db.insert(TABLE, values);
        }
        cursor.close();

    }

    public static final class Builder {
        private final ContentValues values = new ContentValues();

        public Builder id(long id) {
            values.put(_ID, id);
            return this;
        }

        public Builder rockyRequestId(long id) {
            values.put(ROCKY_REQUEST_ID, id);
            return this;
        }

        public Builder type(Type type) {
            values.put(TYPE, type.toString());
            return this;
        }

        public Builder stringValue(String stringValue) {
            values.put(STRING_VALUE, stringValue);
            return this;
        }

        public ContentValues build() {
            return values;
        }

    }

    public enum Type {
        LANGUAGE_PREF("preferred_language");

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
