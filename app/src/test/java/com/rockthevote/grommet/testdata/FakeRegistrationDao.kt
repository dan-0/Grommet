package com.rockthevote.grommet.testdata

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.rockthevote.grommet.data.db.dao.RegistrationDao
import com.rockthevote.grommet.data.db.model.Registration

class FakeRegistrationDao : RegistrationDao {
    var insertHandler: ((Array<out Registration>) -> Unit)? = null

    @Query("SELECT * FROM registration")
    override suspend fun getAll(): List<Registration> {
        TODO("Not implemented")
    }

    @Insert
    override suspend fun insert(vararg registrations: Registration) {
        insertHandler?.invoke(registrations)
    }

    @Delete
    override suspend fun delete(vararg registrations: Registration) {
        TODO("not implemented")
    }
}