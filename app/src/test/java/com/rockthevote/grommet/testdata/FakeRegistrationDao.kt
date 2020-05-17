package com.rockthevote.grommet.testdata

import com.rockthevote.grommet.data.db.dao.RegistrationDao
import com.rockthevote.grommet.data.db.model.Registration

class FakeRegistrationDao : RegistrationDao {
    var insertHandler: ((Array<out Registration>) -> Unit)? = null

    override suspend fun getAll(): List<Registration> {
        TODO("Not implemented")
    }

    override suspend fun insert(vararg registrations: Registration) {
        insertHandler?.invoke(registrations)
    }

    override suspend fun delete(vararg registrations: Registration) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}