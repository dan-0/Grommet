package com.rockthevote.grommet.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.rockthevote.grommet.data.db.model.AdditionalInfo;
import com.rockthevote.grommet.data.db.model.Address;
import com.rockthevote.grommet.data.db.model.ContactMethod;
import com.rockthevote.grommet.data.db.model.Name;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.rockthevote.grommet.data.db.model.VoterClassification;
import com.rockthevote.grommet.data.db.model.VoterId;

public class DbOpenHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;

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
            + RockyRequest.PARTNER_OPT_IN_EMAIL + " INTEGER DEFAULT " + Db.BOOLEAN_FALSE + ","
            + RockyRequest.SOURCE_TRACKING_ID + " TEXT,"
            + RockyRequest.PARTNER_TRACKING_ID + " TEXT,"
            + RockyRequest.OPEN_TRACKING_ID + " TEXT,"
            + RockyRequest.GENERATED_DATE + " TEXT NOT NULL,"
            + RockyRequest.DATE_OF_BIRTH + " TEXT,"
            + RockyRequest.REG_IS_MAIL + " INTEGER DEFAULT " + Db.BOOLEAN_TRUE + ","
            + RockyRequest.RACE + " TEXT,"
            + RockyRequest.PARTY + " TEXT,"
            + RockyRequest.SIGNATURE + " BLOB,"
            + RockyRequest.LATITUDE + " REAL,"
            + RockyRequest.LONGITUDE + " REAL"
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

    public DbOpenHelper(Context context) {
        super(context, "grommet.db", null, VERSION);
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
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
        super.onConfigure(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }
}
