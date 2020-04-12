package com.rockthevote.grommet.data

import android.app.Application
import androidx.room.Room
import com.rockthevote.grommet.data.db.GrommetDatabase
import com.rockthevote.grommet.ui.registration.BaseRegistrationFragment
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(
    injects = [BaseRegistrationFragment::class],
    complete = false
)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideRoomDatabase(app: Application): GrommetDatabase {
        return Room.databaseBuilder(app, GrommetDatabase::class.java, "grommet-db")
            // TODO we may not want this option for production, but good to have while we're deciding on DB structure
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideRegistrationDao(db: GrommetDatabase) = db.registrationDao()
}