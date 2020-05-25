package com.rockthevote.grommet.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rockthevote.grommet.data.db.model.Session
import com.rockthevote.grommet.data.db.relationship.SessionWithPartnerInfo
import com.rockthevote.grommet.data.db.relationship.SessionWithRegistrations

/**
 * Created by Mechanical Man on 4/18/20.
 */
@Dao
interface SessionDao {
    @Query("SELECT * FROM session")
    fun getAll(): List<Session>

    @Query("SELECT * FROM session LIMIT 1")
    fun getCurrentSession(): Session?

    @Transaction
    @Query("SELECT * FROM session")
    fun getSessionWithPartnerInfo(): LiveData<SessionWithPartnerInfo>

    @Transaction
    @Query("SELECT * FROM session")
    fun getSessionWithRegistrations(): List<SessionWithRegistrations>

    @Insert(onConflict = OnConflictStrategy.REPLACE) // there can be only one
    suspend fun insert(vararg session: Session)

    @Query("DELETE FROM session")
    suspend fun clearAllSessionInfo()

    @Update
    suspend fun update(session: Session)
}