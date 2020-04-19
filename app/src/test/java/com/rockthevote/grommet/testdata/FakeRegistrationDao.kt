package com.rockthevote.grommet.testdata

import com.rockthevote.grommet.data.db.dao.RegistrationDao
import com.rockthevote.grommet.data.db.model.Registration

class FakeRegistrationDao : RegistrationDao {
    var insertHandler: ((Array<out Registration>) -> Unit)? = null

    override fun getAll(): List<Registration> {
        TODO("Not implemented")
    }

    override fun insert(vararg registration: Registration) {
        insertHandler?.invoke(registration)
    }

    override fun delete(registration: Registration) {
        TODO("Not implemented")
    }
}