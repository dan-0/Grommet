package com.rockthevote.grommet.data.db.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.rockthevote.grommet.data.db.Db;
import com.rockthevote.grommet.util.Strings;
import com.squareup.sqlbrite.BriteDatabase;

import rx.functions.Func1;

@AutoValue
public abstract class Address implements Parcelable, BaseColumns {
    public static final String TABLE = "address";

    public static final String ROCKY_REQUEST_ID = "rocky_request_id";
    public static final String TYPE = "type";
    public static final String STREET_NAME = "street_name";
    public static final String SUB_ADDRESS = "sub_address";
    public static final String SUB_ADDRESS_TYPE = "sub_address_type";
    public static final String MUNICIPAL_JURISDICTION = "municipal_jurisdiction";
    public static final String COUNTY = "county";
    public static final String STATE = "state";
    public static final String ZIP = "zip";

    public static final String SELECT_BY_TYPE = ""
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

    @Nullable
    public abstract String streetName();

    @Nullable
    public abstract String subAddress();

    @Nullable
    public abstract String subAddressType();

    @Nullable
    public abstract String municipalJurisdiction();

    @Nullable
    public abstract String county();

    @Nullable
    public abstract String state();

    @Nullable
    public abstract String zip();

    public static final Func1<Cursor, Address> MAPPER = new Func1<Cursor, Address>() {
        @Override
        public Address call(Cursor cursor) {
            long id = Db.getLong(cursor, _ID);
            long rockyRequestId = Db.getLong(cursor, ROCKY_REQUEST_ID);
            Type type = Type.fromString(Db.getString(cursor, TYPE));
            String streetName = Db.getString(cursor, STREET_NAME);
            String subAddress = Db.getString(cursor, SUB_ADDRESS);
            String subAddressType = Db.getString(cursor, SUB_ADDRESS_TYPE);
            String munJurisdiction = Db.getString(cursor, MUNICIPAL_JURISDICTION);
            String county = Db.getString(cursor, COUNTY);
            String state = Db.getString(cursor, STATE);
            String zip = Db.getString(cursor, ZIP);

            return new AutoValue_Address(id, rockyRequestId, type, streetName, subAddress,
                    subAddressType, munJurisdiction, county, state, zip);
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

        public Builder streetName(String streetName) {
            values.put(STREET_NAME, streetName);
            return this;
        }

        public Builder subAddress(String subAdd) {
            values.put(SUB_ADDRESS, subAdd);
            return this;
        }

        public Builder subAddressType(String subAddType) {
            values.put(SUB_ADDRESS_TYPE, subAddType);
            return this;
        }

        public Builder municipalJurisdiction(String municipalJurisdiction) {
            values.put(MUNICIPAL_JURISDICTION, municipalJurisdiction);
            return this;
        }

        public Builder county(String county) {
            values.put(COUNTY, county);
            return this;
        }

        public Builder state(String state) {
            values.put(STATE, state);
            return this;
        }

        public Builder zip(String zip) {
            values.put(ZIP, zip);
            return this;
        }

        public ContentValues build() {
            return values;
        }

    }

    public enum Type {
        MAILING_ADDRESS("mailing_address"),
        PREVIOUS_ADDRESS("previous_address"),
        REGISTRATION_ADDRESS("registration_address"),
        ASSISTANT_ADDRESS("assistant_address");

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
            return MAILING_ADDRESS;
        }
    }

    @Override
    public String toString() {
        return Strings.valueOrDefault(streetName() + "\n","")
                + Strings.valueOrDefault(subAddress() + "\n", "")
                + Strings.valueOrDefault(municipalJurisdiction() + ", ", "")
                + Strings.valueOrDefault(state() + " ", "")
                + Strings.valueOrDefault(zip() + "\n","");
    }
}
