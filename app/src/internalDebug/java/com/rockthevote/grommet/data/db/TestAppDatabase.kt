package com.rockthevote.grommet.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import com.rockthevote.grommet.data.db.AppDatabase
import com.rockthevote.grommet.data.db.dao.RegistrationDao
import com.rockthevote.grommet.data.db.model.Registration

/**
 * Created by Mechanical Man on 3/24/20.
 */

@Database(entities = [Registration::class], version = 1, exportSchema = false)
abstract class TestAppDatabase : AppDatabase() {

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance
                    ?: synchronized(this) {
                        instance
                                ?: Room.inMemoryDatabaseBuilder(
                                context,
                                AppDatabase::class.java
                        ).build()
                    }
        }
    }
}