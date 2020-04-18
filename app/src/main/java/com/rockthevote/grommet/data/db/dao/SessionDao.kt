package com.rockthevote.grommet.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.rockthevote.grommet.data.db.model.Session

/**
 * Created by Mechanical Man on 4/18/20.
 */
@Dao
interface SessionDao {
    @Query("SELECT * FROM session")
    fun getAll(): List<Session>
}