package com.rockthevote.grommet.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.rockthevote.grommet.data.db.model.AdditionalInfo;
import com.rockthevote.grommet.data.db.model.Address;
import com.rockthevote.grommet.data.db.model.ContactMethod;
import com.rockthevote.grommet.data.db.model.Name;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.rockthevote.grommet.data.db.model.Session;
import com.rockthevote.grommet.data.db.model.VoterClassification;
import com.rockthevote.grommet.data.db.model.VoterId;

import timber.log.Timber;

public class DbOpenHelper extends SQLiteOpenHelper {

    private static final int INITIAL_VERSION = 1;
    private static final int VER_JULY_2017_RELEASE_A = 200;
    private static final int VER_JANUARY_2018_RELEASE_A = 300;
    private static final int VER_MARCH_2018_RELEASE_A = 400;
    private static final int VER_JULY_2018_RELEASE_A = 500;

    private static final int CUR_DATABASE_VERSION = VER_JULY_2018_RELEASE_A;

    /**
     * Only model the relations, not the objects
     */

    public static final String CREATE_ROCKY_REQUEST = ""
            + "CREATE TABLE " + RockyRequest.TABLE + "("
            + RockyRequest._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + RockyRequest.STATUS + " TEXT NOT NULL,"
            + RockyRequest.LANGUAGE + " TEXT,"
            + RockyRequest.PHONE_TYPE + " TEXT,"
            + RockyRequest.PARTNER_ID + " TEXT NOT NULL,"
            + RockyRequest.OPT_IN_EMAIL + " INTEGER DEFAULT " + Db.BOOLEAN_FALSE + ","
            + RockyRequest.OPT_IN_SMS + " INTEGER DEFAULT " + Db.BOOLEAN_FALSE + ","
            + RockyRequest.OPT_IN_VOLUNTEER + " INTEGER DEFAULT " + Db.BOOLEAN_FALSE + ","
            + RockyRequest.PARTNER_OPT_IN_SMS + " INTEGER DEFAULT " + Db.BOOLEAN_FALSE + ","
            + RockyRequest.PARTNER_OPT_IN_EMAIL + " INTEGER DEFAULT " + Db.BOOLEAN_TRUE + ","
            + RockyRequest.SOURCE_TRACKING_ID + " TEXT,"
            + RockyRequest.PARTNER_TRACKING_ID + " TEXT,"
            + RockyRequest.OPEN_TRACKING_ID + " TEXT,"
            + RockyRequest.GENERATED_DATE + " TEXT NOT NULL,"
            + RockyRequest.DATE_OF_BIRTH + " TEXT,"
            + RockyRequest.HAS_MAILING_ADDRESS + " INTEGER DEFAULT " + Db.BOOLEAN_FALSE + ","
            + RockyRequest.RACE + " TEXT,"
            + RockyRequest.PARTY + " TEXT,"
            + RockyRequest.SIGNATURE + " BLOB,"
            + RockyRequest.LATITUDE + " REAL,"
            + RockyRequest.LONGITUDE + " REAL,"
            + RockyRequest.HAS_PREVIOUS_NAME + " INTEGER DEFAULT " + Db.BOOLEAN_FALSE + ","
            + RockyRequest.HAS_PREVIOUS_ADDRESS + " INTEGER DEFAULT " + Db.BOOLEAN_FALSE + ","
            + RockyRequest.HAS_ASSISTANT + " INTEGER DEFAULT " + Db.BOOLEAN_FALSE + ","
            + RockyRequest.OTHER_PARTY + " TEXT "
            + ")";

    private static final String CREATE_ADDRESS = ""
            + "CREATE TABLE " + Address.TABLE + "("
            + Address._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + Address.ROCKY_REQUEST_ID + " INTEGER NOT NULL,"
            + Address.TYPE + " TEXT NOT NULL,"
            + Address.STREET_NAME + " TEXT,"
            + Address.SUB_ADDRESS + " TEXT,"
            + Address.MUNICIPAL_JURISDICTION + " TEXT,"
            + Address.COUNTY + " TEXT,"
            + Address.STATE + " TEXT,"
            + Address.ZIP + " TEXT,"
            + "UNIQUE (" + Address.ROCKY_REQUEST_ID + " , " + Address.TYPE + "),"
            + "FOREIGN KEY (" + Address.ROCKY_REQUEST_ID + ") REFERENCES " + RockyRequest.TABLE + "(" + RockyRequest._ID + ") ON DELETE CASCADE "
            + ")";

    private static final String CREATE_NAME = ""
            + "CREATE TABLE " + Name.TABLE + "("
            + Name._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + Name.ROCKY_REQUEST_ID + " INTEGER NOT NULL,"
            + Name.TYPE + " TEXT NOT NULL,"
            + Name.FIRST_NAME + " TEXT,"
            + Name.LAST_NAME + " TEXT,"
            + Name.MIDDLE_NAME + " TEXT,"
            + Name.TITLE_PREFIX + " TEXT NOT NULL DEFAULT " + Name.Prefix.MR.toString() + ","
            + Name.TITLE_SUFFIX + " TEXT,"
            + "UNIQUE (" + Name.ROCKY_REQUEST_ID + " , " + Name.TYPE + "),"
            + "FOREIGN KEY (" + Name.ROCKY_REQUEST_ID + ") REFERENCES " + RockyRequest.TABLE + "(" + RockyRequest._ID + ") ON DELETE CASCADE "
            + ")";

    private static final String CREATE_VOTER_CLASSIFICATION = ""
            + "CREATE TABLE " + VoterClassification.TABLE + "("
            + VoterClassification._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + VoterClassification.ROCKY_REQUEST_ID + " INTEGER NOT NULL,"
            + VoterClassification.TYPE + " TEXT,"
            + VoterClassification.ASSERTION + " INTEGER,"
            + "UNIQUE (" + VoterClassification.ROCKY_REQUEST_ID + " , " + VoterClassification.TYPE + "),"
            + "FOREIGN KEY (" + VoterClassification.ROCKY_REQUEST_ID + ") REFERENCES " + RockyRequest.TABLE + "(" + RockyRequest._ID + ") ON DELETE CASCADE "
            + ")";

    private static final String CREATE_VOTER_ID = ""
            + "CREATE TABLE " + VoterId.TABLE + "("
            + VoterId._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + VoterId.ROCKY_REQUEST_ID + " INTEGER NOT NULL,"
            + VoterId.TYPE + " TEXT NOT NULL,"
            + VoterId.VALUE + " TEXT,"
            + VoterId.ATTEST_NO_SUCH_ID + " INTEGER,"
            + "UNIQUE (" + VoterId.ROCKY_REQUEST_ID + " , " + VoterId.TYPE + "),"
            + "FOREIGN KEY (" + VoterId.ROCKY_REQUEST_ID + ") REFERENCES " + RockyRequest.TABLE + "(" + RockyRequest._ID + ") ON DELETE CASCADE "
            + ")";

    private static final String CREATE_CONTACT_METHOD = ""
            + "CREATE TABLE " + ContactMethod.TABLE + "("
            + ContactMethod._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + ContactMethod.ROCKY_REQUEST_ID + " INTEGER NOT NULL,"
            + ContactMethod.TYPE + " TEXT NOT NULL,"
            + ContactMethod.VALUE + " TEXT,"
            + "UNIQUE (" + ContactMethod.ROCKY_REQUEST_ID + " , " + ContactMethod.TYPE + "),"
            + "FOREIGN KEY (" + ContactMethod.ROCKY_REQUEST_ID + ") REFERENCES " + RockyRequest.TABLE + "(" + RockyRequest._ID + ") ON DELETE CASCADE "
            + ")";

    private static final String CREATE_ADDITIONAL_INFO = ""
            + "CREATE TABLE " + AdditionalInfo.TABLE + "("
            + AdditionalInfo._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + AdditionalInfo.ROCKY_REQUEST_ID + " INTEGER NOT NULL,"
            + AdditionalInfo.TYPE + " TEXT NOT NULL,"
            + AdditionalInfo.STRING_VALUE + " TEXT,"
            + "FOREIGN KEY (" + AdditionalInfo.ROCKY_REQUEST_ID + ") REFERENCES " + RockyRequest.TABLE + "(" + RockyRequest._ID + ") ON DELETE CASCADE "
            + ")";

    private static final String CREATE_SESSION_TABLE = ""
            + "CREATE TABLE " + Session.TABLE + "("
            + Session._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + Session.SESSION_ID + " TEXT NOT NULL,"
            + Session.SESSION_STATUS + " TEXT DEFAULT " + Session.SessionStatus.SESSION_CLEARED + ","
            + Session.CLOCK_IN_TIME + " TEXT,"
            + Session.CLOCK_OUT_TIME + " TEXT,"
            + Session.CLOCK_IN_REPORTED + " INTEGER DEFAULT " + Db.BOOLEAN_FALSE + ","
            + Session.CLOCK_OUT_REPORTED + " INTEGER DEFAULT " + Db.BOOLEAN_FALSE + ","
            + Session.CANVASSER_NAME + " TEXT,"
            + Session.PARTNER_TRACKING_ID + " INTEGER,"
            + Session.OPEN_TRACKING_ID + " INTEGER,"
            + Session.LATITUDE + " REAL,"
            + Session.LONGITUDE + " REAL,"
            + Session.SESSION_TIMEOUT + " INTEGER DEFAULT 0,"
            + Session.TOTAL_REGISTRATIONS + " INTEGER DEFAULT 0,"
            + Session.TOTAL_ABANDONED + " INTEGER DEFAULT 0,"
            + Session.TOTAL_EMAIL_OPT_IN + " INTEGER DEFAULT 0,"
            + Session.TOTAL_SMS_OPT_IN + " INTEGER DEFAULT 0,"
            + Session.TOTAL_INCLUDE_DLN + " INTEGER DEFAULT 0,"
            + Session.TOTAL_INCLUDE_SSN + " INTEGER DEFAULT 0"
            + ")";

    private static final String ADD_SESSION_ID_TO_ROCKY_REQUEST = ""
            + "ALTER TABLE " + RockyRequest.TABLE + " "
            + "ADD COLUMN " + RockyRequest.SESSION_ID + " INTEGER";

    private static final String ADD_UNIT_TYPE_TO_ADDRESS = ""
            + "ALTER TABLE " + Address.TABLE + " "
            + "ADD COLUMN " + Address.SUB_ADDRESS_TYPE + " TEXT";

    private static final String ADD_STREET_NAME_2_TO_ADDRESS = ""
            + "ALTER TABLE " + Address.TABLE + " "
            + "ADD COLUMN " + Address.STREET_NAME_2 + " TEXT";

    private static final String ADD_DEVICE_ID_TO_SESSION = ""
            + "ALTER TABLE " + Session.TABLE + " "
            + "ADD COLUMN " + Session.DEVICE_ID + " TEXT";

    private static final String ADD_PARTNER_OPT_IN_VOLUNTEER_TO_ROCKY_REQUEST = ""
            + "ALTER TABLE " + RockyRequest.TABLE + " "
            + "ADD COLUMN " + RockyRequest.PARTNER_OPT_IN_VOLUNTEER + " INTEGER DEFAULT " + Db.BOOLEAN_FALSE;

    public DbOpenHelper(Context context) {
        super(context, "grommet.db", null, CUR_DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ROCKY_REQUEST);
        db.execSQL(CREATE_ADDRESS);
        db.execSQL(CREATE_NAME);
        db.execSQL(CREATE_VOTER_CLASSIFICATION);
        db.execSQL(CREATE_VOTER_ID);
        db.execSQL(CREATE_CONTACT_METHOD);
        db.execSQL(CREATE_ADDITIONAL_INFO);

        upgradeFromInitialVersionToJuly2017A(db);
        upgradeFromJuly2017AToJanuary2018A(db);
        upgradeFromJanuary2018AToMarch2018A(db);
        upgradeFromMarch2018AToJuly2018A(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
        super.onConfigure(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int version = oldVersion;

        if (version == INITIAL_VERSION) {
            Timber.d("upgrading from %d to %d", oldVersion, newVersion);
            upgradeFromInitialVersionToJuly2017A(db);
            version = VER_JULY_2017_RELEASE_A;
        }

        if (version == VER_JULY_2017_RELEASE_A) {
            Timber.d("upgrading from %d to %d", oldVersion, newVersion);
            upgradeFromJuly2017AToJanuary2018A(db);
            version = VER_JANUARY_2018_RELEASE_A;
        }

        if (version == VER_JANUARY_2018_RELEASE_A) {
            Timber.d("upgrading from %d to %d", oldVersion, newVersion);
            upgradeFromJanuary2018AToMarch2018A(db);
            version = VER_MARCH_2018_RELEASE_A;
        }

        if (version == VER_MARCH_2018_RELEASE_A) {
            Timber.d("upgrading from %d to %d", oldVersion, newVersion);
            upgradeFromMarch2018AToJuly2018A(db);
            version = VER_JULY_2018_RELEASE_A;
        }

    }

    private static void upgradeFromInitialVersionToJuly2017A(SQLiteDatabase db) {
        db.execSQL(CREATE_SESSION_TABLE);
        db.execSQL(ADD_SESSION_ID_TO_ROCKY_REQUEST);

    }

    private static void upgradeFromJuly2017AToJanuary2018A(SQLiteDatabase db) {
        db.execSQL(ADD_UNIT_TYPE_TO_ADDRESS);
    }

    private static void upgradeFromJanuary2018AToMarch2018A(SQLiteDatabase db) {
        db.execSQL(ADD_STREET_NAME_2_TO_ADDRESS);
    }

    private static void upgradeFromMarch2018AToJuly2018A(SQLiteDatabase db) {
        db.execSQL(ADD_DEVICE_ID_TO_SESSION);
        db.execSQL(ADD_PARTNER_OPT_IN_VOLUNTEER_TO_ROCKY_REQUEST);
    }
}

