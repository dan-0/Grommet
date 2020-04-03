package com.rockthevote.grommet.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

/**
 * Created by Mechanical Man on 4/1/20.
 */
@Dao
interface RegistrationDao {
    @Query("SELECT * FROM registration")
    fun getAll(): List<Registration>

    @Insert
    fun insert(registration: Registration)

    @Delete
    fun delete(registration: Registration)
}
