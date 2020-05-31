package com.rockthevote.grommet.ui.eventFlow

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao
import com.rockthevote.grommet.data.db.dao.SessionDao
import com.rockthevote.grommet.util.coroutines.DispatcherProvider
import com.rockthevote.grommet.util.coroutines.DispatcherProviderImpl

/**
 * Created by Mechanical Man on 5/30/20.
 */
class SessionTimeTrackingViewModel(
        private val dispatchers: DispatcherProvider = DispatcherProviderImpl(),
        private val partnerInfoDao: PartnerInfoDao,
        private val sessionDao: SessionDao
) : ViewModel() {

    val sessionData = Transformations.map(partnerInfoDao.getPartnerInfoWithSessionAndRegistrations()) { result ->
        result?.let{
            val partnerInfo = result.partnerInfo
            val session = result.sessionWithRegistrations?.session
            val registrations = result.sessionWithRegistrations?.registrations

            SessionSummaryData(
                    partnerInfo?.partnerName ?: "",
                    session?.canvasserName ?: "",
                    session?.openTrackingId ?: "",
                    session?.partnerTrackingId ?: "",
                    session?.deviceId ?: "",
                     session?.smsCount ?: 0,
                    session?.driversLicenseCount ?: 0,
                    session?.ssnCount ?:0,
                    session?.emailCount ?: 0,
                    session?.registrationCount ?: 0,
                    session?.abandonedCount ?: 0,
                    registrations ?: emptyList(),
                    session?.clockInTime

            )
        } ?: run {
            SessionSummaryData()
        }
    }


}

class SessionTimeTrackingViewModelFactory(
        private val partnerInfoDao: PartnerInfoDao,
        private val sessionDao: SessionDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val dispatchers = DispatcherProviderImpl()

        @Suppress("UNCHECKED_CAST")
        return SessionTimeTrackingViewModel(dispatchers, partnerInfoDao, sessionDao) as T
    }
}