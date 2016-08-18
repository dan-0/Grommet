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
public abstract class VoterId implements Parcelable, BaseColumns {
    public static final String TABLE = "voter_id";

    public static String ROCKY_REQUEST_ID = "rocky_request_id";
    public static String TYPE = "type";
    public static String VALUE = "value";
    public static String ATTEST_NO_SUCH_ID = "attest_no_such_id";

    private static final String SELECT_BY_TYPE = ""
            + "SELECT * FROM "
            + TABLE
            + " WHERE "
            + ROCKY_REQUEST_ID + " = ? "
            + " AND "
            + TYPE + " = ? "
            + " LIMIT 1";

    public static final String SELECT_BY_ROCKY_REQUEST_ID = ""
            + "SELECT * FROM "
            + TABLE
            + " WHERE "
            + ROCKY_REQUEST_ID + " = ? ";

    public abstract long id();

    public abstract long rockyRequestId();

    public abstract Type type();

    public abstract String value();

    public abstract boolean attestNoSuchId();

    public static final Func1<Cursor, VoterId> MAPPER = new Func1<Cursor, VoterId>() {
        @Override
        public VoterId call(Cursor cursor) {
            long id = Db.getLong(cursor, _ID);
            long rockyRequestId = Db.getLong(cursor, ROCKY_REQUEST_ID);
            Type type = Type.fromString(Db.getString(cursor, TYPE));
            String value = Db.getString(cursor, VALUE);
            boolean attestNoSuchId = Db.getBoolean(cursor, ATTEST_NO_SUCH_ID);

            return new AutoValue_VoterId(id, rockyRequestId, type, value, attestNoSuchId);
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

        public Builder value(String value) {
            values.put(VALUE, value);
            return this;
        }

        public Builder attestNoSuchId(boolean value){
            values.put(ATTEST_NO_SUCH_ID, value);
            return this;
        }

        public ContentValues build() {
            return values;
        }
    }

    public enum Type {
        DRIVERS_LICENSE("drivers_license"),
        SSN_LAST_FOUR("ssn_last_four");

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
