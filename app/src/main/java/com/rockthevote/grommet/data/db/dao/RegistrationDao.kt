package com.rockthevote.grommet.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.rockthevote.grommet.data.db.model.Registration

@Dao
interface RegistrationDao {
    @Query("SELECT * FROM registration")
    fun getAll(): List<Registration>

    @Insert
    fun insert(vararg registration: Registration)

    @Delete
    fun delete(registration: Registration)
}

