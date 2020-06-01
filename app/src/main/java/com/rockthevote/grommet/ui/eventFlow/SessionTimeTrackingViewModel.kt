package com.rockthevote.grommet.ui.eventFlow

import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao
import com.rockthevote.grommet.data.db.dao.RegistrationDao
import com.rockthevote.grommet.data.db.dao.SessionDao
import com.rockthevote.grommet.util.coroutines.DispatcherProvider
import com.rockthevote.grommet.util.coroutines.DispatcherProviderImpl
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Created by Mechanical Man on 5/30/20.
 */
class SessionTimeTrackingViewModel(
    private val dispatchers: DispatcherProvider = DispatcherProviderImpl(),
    private val partnerInfoDao: PartnerInfoDao,
    private val sessionDao: SessionDao,
    private val registrationDao: RegistrationDao
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
    }

    private val _effect = LiveEvent<SessionSummaryState.Effect?>()
    val effect: LiveData<SessionSummaryState.Effect?> = _effect

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

    /**
     * Asynchronously determines if the user can clock out. Calls [successCallback]
     * when the user can logout, [failCallback] when the user cannot.
     *
     * Note: Coroutines cannot be used from Java, so callbacks are necessary
     */
    fun asyncCanClockOut(successCallback: () -> Unit, failCallback: () -> Unit) {
        viewModelScope.launch(dispatchers.io) {
            val canClockOut = registrationDao.getAll().isEmpty()

            withContext(dispatchers.main) {
                if (canClockOut) {
                    successCallback()
                } else {
                    failCallback()
                }
            }
        }
    }

    fun clearSession() {
        viewModelScope.launch(dispatchers.io + coroutineExceptionHandler) {
            sessionDao.clearAllSessionInfo()
            // TODO listen for return value and set error state if it fails?

            updateEffect(SessionSummaryState.Cleared)
        }
    }


    private fun updateEffect(newEffect: SessionSummaryState.Effect) {
        Timber.d("Handling new effect: $newEffect")
        _effect.postValue(newEffect)
    }
}

class SessionTimeTrackingViewModelFactory(
        private val partnerInfoDao: PartnerInfoDao,
        private val sessionDao: SessionDao,
        private val registrationDao: RegistrationDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val dispatchers = DispatcherProviderImpl()

        @Suppress("UNCHECKED_CAST")
        return SessionTimeTrackingViewModel(dispatchers, partnerInfoDao, sessionDao, registrationDao) as T
    }
}