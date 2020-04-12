package com.rockthevote.grommet.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rockthevote.grommet.data.db.dao.RegistrationDao
import com.rockthevote.grommet.data.db.model.Registration

@Database(entities = [Registration::class], version = 2, exportSchema = false)
abstract class GrommetDatabase : RoomDatabase() {
    abstract fun registrationDao(): RegistrationDao
}