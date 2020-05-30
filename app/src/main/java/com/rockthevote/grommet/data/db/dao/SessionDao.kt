package com.rockthevote.grommet.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.rockthevote.grommet.data.db.model.Session
import com.rockthevote.grommet.data.db.relationship.SessionWithPartnerInfo
import com.rockthevote.grommet.data.db.relationship.SessionWithRegistrations
import com.rockthevote.grommet.data.db.relationship.SessionWithRegistrationsAndPartnerInfo

/**
 * Created by Mechanical Man on 4/18/20.
 */
@Dao
interface SessionDao {
    @Query("SELECT * FROM session")
    fun getAll(): List<Session>

    @Query("SELECT * FROM session LIMIT 1")
    fun getCurrentSession(): Session?

    @Query("SELECT * FROM session LIMIT 1")
    fun getCurrentSessionLive(): LiveData<Session?>

    @Transaction
    @Query("SELECT * FROM session")
    fun getSessionWithPartnerInfo(): LiveData<SessionWithPartnerInfo?>

    @Transaction
    @Query("SELECT * FROM session")
    fun getSessionWithRegistrations(): LiveData<SessionWithRegistrations?>

    @Transaction
    @Query("SELECT * FROM session LIMIT 1")
    fun getSessionWithRegistrationsAndPartnerInfo(): LiveData<SessionWithRegistrationsAndPartnerInfo?>

    @Insert
    fun insert(vararg session: Session)

    @Query("DELETE FROM session")
    fun clearAllSessionInfo()
}