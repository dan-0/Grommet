package com.rockthevote.grommet.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.rockthevote.grommet.data.db.model.Session
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
    fun getSessionWithRegistrations(): List<SessionWithRegistrations>
}