package com.rockthevote.grommet.data.db.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.rockthevote.grommet.data.db.Db;
import com.rockthevote.grommet.util.Strings;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.Locale;

import rx.functions.Func1;

@AutoValue
public abstract class Name implements Parcelable, BaseColumns {
    public static final String TABLE = "name";

    public static final String ROCKY_REQUEST_ID = "rocky_request_id";
    public static final String TYPE = "type";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String MIDDLE_NAME = "middle_name";
    public static final String TITLE_PREFIX = "title_prefix";
    public static final String TITLE_SUFFIX = "title_suffix";

    public static final String SELECT_BY_TYPE = ""
            + "SELECT * FROM "
            + TABLE
            + " WHERE "
            + ROCKY_REQUEST_ID + "=?"
            + " AND "
            + TYPE + "=?"
            + " LIMIT 1";

    public static final String DELETE_BY_TYPE = ""
            + TYPE + "=? "
            + " AND " + ROCKY_REQUEST_ID + "=?";

    public abstract long id();

    public abstract long rockyRequestId();

    public abstract Type type();

    @Nullable
    public abstract String firstName();

    @Nullable
    public abstract String lastName();

    @Nullable
    public abstract String middleName();

    @Nullable
    public abstract Prefix titlePrefix();

    @Nullable
    public abstract Suffix titleSuffix();

    public static final Func1<Cursor, Name> MAPPER = new Func1<Cursor, Name>() {
        @Override
        public Name call(Cursor cursor) {
            long id = Db.getLong(cursor, _ID);
            long rockyRequestId = Db.getLong(cursor, ROCKY_REQUEST_ID);
            Type type = Type.fromString(Db.getString(cursor, TYPE));
            String firstName = Db.getString(cursor, FIRST_NAME);
            String lastName = Db.getString(cursor, LAST_NAME);
            String middleName = Db.getString(cursor, MIDDLE_NAME);
            Prefix prefix = Prefix.fromString(Db.getString(cursor, TITLE_PREFIX));
            Suffix suffix = Suffix.fromString(Db.getString(cursor, TITLE_SUFFIX));

            return new AutoValue_Name(id, rockyRequestId, type, firstName, lastName,
                    middleName, prefix, suffix);
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

        public Builder firstName(String firstName) {
            values.put(FIRST_NAME, firstName);
            return this;
        }

        public Builder lastName(String lastName) {
            values.put(LAST_NAME, lastName);
            return this;
        }

        public Builder middleName(String middleName) {
            values.put(MIDDLE_NAME, middleName);
            return this;
        }

        public Builder prefix(Prefix prefix) {
            values.put(TITLE_PREFIX, prefix.toString());
            return this;
        }

        public Builder suffix(Suffix suffix) {
            values.put(TITLE_SUFFIX, suffix.toString());
            return this;
        }

        public ContentValues build() {
            return values;
        }

    }

    public enum Type {
        CURRENT_NAME("current_name"),
        PREVIOUS_NAME("previous_name"),
        ASSISTANT_NAME("assistant_name");

        private final String type;

        Type(String type) {
            this.type = type;
        }

        @Override
        @NonNull
        public String toString() {
            return type;
        }

        public static Type fromString(String type) {
            for (Type val : values()) {
                if (val.toString().equals(type)) {
                    return val;
                }
            }
            return CURRENT_NAME;
        }
    }

    public enum Suffix {
        EMPTY(""), JR("Jr"), SR("Sr"), SECOND("II"), THIRD("III"), FOURTH("IV"),
        FIFTH("V"), SIXTH("VI"), SEVENTH("VII");

        private final String suffix;

        Suffix(String suffix) {
            this.suffix = suffix;
        }

        @Override
        @NonNull
        public String toString() {
            return suffix;
        }

        @NonNull
        public static Suffix fromString(String suffix) {
            for (Suffix value : Suffix.values()) {
                if (value.suffix.equals(suffix)) {
                    return value;
                }
            }
            return EMPTY;
        }

    }

    public enum Prefix {
        MR("Mr", "Sr"),
        MS("Ms", "Ms"), // intentionally the same
        MRS("Mrs", "Srta"),
        MISS("Miss", "Sra");

        private final String enTitle;
        private final String esTitle;

        Prefix(String enTitle, String esTitle) {
            this.enTitle = enTitle;
            this.esTitle = esTitle;
        }

        @Override
        @NonNull
        public String toString() {
            if ("es".equals(Locale.getDefault().getLanguage())) {
                return esTitle;
            } else {
                // default to english
                return enTitle;
            }
        }

        @NonNull
        public static Prefix fromString(String title) {
            for (Prefix value : Prefix.values()) {
                if (value.enTitle.equals(title) ||
                        value.esTitle.equals(title)) {
                    return value;
                }
            }
            return MR;
        }

    }

    public enum Gender {
        MALE("male"), FEMALE("female");

        private final String gender;

        Gender(String gender) {
            this.gender = gender;
        }

        @Override
        @NonNull
        public String toString() {
            return gender;
        }

        @NonNull
        public static Gender fromString(String gender) {
            for (Gender value : Gender.values()) {
                if (value.gender.equals(gender)) {
                    return value;
                }
            }
            return MALE;
        }

        @NonNull
        public static Gender fromPrefix(Prefix prefix) {
            switch (prefix) {
                case MS:
                case MRS:
                case MISS:
                    return FEMALE;
                case MR:
                default:
                    return MALE;
            }
        }
    }

    @Override
    public String toString() {
        return Strings.valueOrDefault(titlePrefix().toString() + " ", "")
                + Strings.valueOrDefault(firstName() + " ", "")
                + Strings.valueOrDefault(middleName() + " ", "")
                + Strings.valueOrDefault(lastName() + " ", "")
                + Strings.valueOrDefault(titleSuffix().toString(), "");
    }
}
