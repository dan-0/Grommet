package com.rockthevote.grommet.ui.eventFlow

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rockthevote.grommet.data.db.dao.SessionDao
import com.rockthevote.grommet.data.db.relationship.SessionWithRegistrations
import com.rockthevote.grommet.util.coroutines.DispatcherProvider
import com.rockthevote.grommet.util.coroutines.DispatcherProviderImpl

/**
 * Created by Mechanical Man on 5/30/20.
 */
class SessionTimeTrackingViewModel(
        private val dispatchers: DispatcherProvider = DispatcherProviderImpl(),
        private val sessionDao: SessionDao
) : ViewModel() {

    val sessionData = Transformations.map(sessionDao.getSessionWithRegistrationsAndPartnerInfo()){ result ->
        result?.let{
            val partnerInfo = result.partnerInfo
            val session = result.sessionWithRegistrations?.session

            SessionSummaryData(
                    partnerInfo?.partnerName ?: "",
                    session?.canvasserName ?: "",
                    session?.openTrackingId ?: "",
                    session?.partnerTrackingId ?: "",
                    session?.deviceId ?: "",
                     session?.smsCount ?: 0,
                    session?.driversLicenseCount ?: 0,
                    session?.ssnCount ?:0,
                    session.
            )
        } ?: run {
            SessionSummaryData()
        }
    }


}

class SessionTimeTrackingViewModelFactory(
        private val sessionDao: SessionDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val dispatchers = DispatcherProviderImpl()

        @Suppress("UNCHECKED_CAST")
        return SessionTimeTrackingViewModel(dispatchers, sessionDao) as T
    }
}