package com.rockthevote.grommet.data.db.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.rockthevote.grommet.data.db.Db;
import com.squareup.sqlbrite.BriteDatabase;

import rx.functions.Func1;

@AutoValue
public abstract class ContactMethod implements Parcelable, BaseColumns {

    public static final String TABLE = "contact_method";

    public static final String ROCKY_REQUEST_ID = "rocky_request_id";
    public static final String TYPE = "type";
    public static final String VALUE = "value";

    public static final String SELECT_BY_TYPE
            = "" + "SELECT * FROM "
            + TABLE
            + " WHERE "
            + ROCKY_REQUEST_ID + "=?"
            + " AND "
            + TYPE + "=? "
            + " LIMIT 1";

    public static final String SELECT_BY_ROCKY_REQUEST_ID = ""
            + "SELECT * FROM "
            + TABLE
            + " WHERE "
            + ROCKY_REQUEST_ID + "=?";

    public abstract long id();

    public abstract long rockyRequestId();

    public abstract Type type();

    public abstract String value();

    public transient static final Func1<Cursor, ContactMethod> MAPPER = new Func1<Cursor, ContactMethod>() {
        @Override
        public ContactMethod call(Cursor cursor) {
            long id = Db.getLong(cursor, _ID);
            long rockyRequestId = Db.getLong(cursor, ROCKY_REQUEST_ID);
            Type type = Type.fromString(Db.getString(cursor, TYPE));
            String value = Db.getString(cursor, VALUE);
            return new AutoValue_ContactMethod(id, rockyRequestId, type, value);
        }
    };

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

        public Builder value(String value) {
            values.put(VALUE, value);
            return this;
        }

        public ContentValues build() {
            return values;
        }
    }

    public static void insertOrUpdate(BriteDatabase db, long rockyRequestId,
                                      ContactMethod.Type type, ContentValues values) {

        values.put(ROCKY_REQUEST_ID, rockyRequestId);
        values.put(TYPE, type.toString());

        Cursor cursor = db.query(
                SELECT_BY_TYPE,
                String.valueOf(rockyRequestId),
                type.toString());

        if (cursor.moveToNext()) {
            long rowId = Db.getLong(cursor, _ID);
            db.update(TABLE, values, _ID + " = ? ", String.valueOf(rowId));
        } else {
            db.insert(TABLE, values);
        }
        cursor.close();

    }

    public enum Type {
        PHONE("phone"), EMAIL("email");

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

    public enum Capability {
        VOICE("voice"), FAX("fax"), SMS("sms");

        private final String capability;

        Capability(String capability) {
            this.capability = capability;
        }

        @Override
        public String toString() {
            return capability;
        }

        @Nullable
        public static Capability fromString(String capability) {
            for (Capability val : values()) {
                if (val.toString().equals(capability)) {
                    return val;
                }
            }
            return null;
        }
    }
}
