package com.rockthevote.grommet.testdata

import androidx.lifecycle.LiveData
import com.rockthevote.grommet.data.db.dao.SessionDao
import com.rockthevote.grommet.data.db.model.Session
import com.rockthevote.grommet.data.db.relationship.SessionWithPartnerInfo
import com.rockthevote.grommet.data.db.relationship.SessionWithRegistrations

class FakeSessionDao(
    private val getCurrentSessionResult: MutableList<Session?> = mutableListOf()
) : SessionDao {
    override fun getAll(): List<Session> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCurrentSession() = getCurrentSessionResult.removeAt(0)
    override fun getSessionWithPartnerInfo(): LiveData<SessionWithPartnerInfo?> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSessionWithRegistrations(): List<SessionWithRegistrations> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun insert(vararg session: Session) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clearAllSessionInfo() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}