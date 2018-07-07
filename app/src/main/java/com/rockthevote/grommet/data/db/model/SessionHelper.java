package com.rockthevote.grommet.data.db.model;

import android.database.Cursor;

import com.squareup.sqlbrite.BriteDatabase;

import static com.rockthevote.grommet.data.db.model.Session.SessionStatus.NEW_SESSION;

/**
 * Created by Mechanical Man on 7/15/18.
 */
public class SessionHelper {

    public static Session.SessionStatus getStatus(BriteDatabase db) {

        Cursor cursor = db.query(Session.SELECT_CURRENT_SESSION);
        int rows = cursor.getCount();
        cursor.close();

        if (rows == 0) {
            return NEW_SESSION;
        } else {

            cursor = db.query(Session.SELECT_CURRENT_SESSION);
            cursor.moveToNext();
            Session session = Session.MAPPER.call(cursor);
            cursor.close();

            return session.sessionStatus();
        }
    }
}
