package com.rockthevote.grommet.ui.eventFlow

import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import com.rockthevote.grommet.data.api.RockyService
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao
import com.rockthevote.grommet.data.db.model.PartnerInfo
import com.rockthevote.grommet.util.coroutines.DispatcherProvider
import com.rockthevote.grommet.util.coroutines.DispatcherProviderImpl
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Created by Mechanical Man on 5/16/20.
 */
class PartnerLoginViewModel(
        private val dispatchers: DispatcherProvider = DispatcherProviderImpl(),
        private val rockyService: RockyService,
        private val partnerInfoDao: PartnerInfoDao
) : ViewModel() {

    val partnerInfoId: LiveData<String> =
            Transformations.map(partnerInfoDao.getCurrentPartnerInfo()) { result ->
                result?.partnerId ?: "-1"
            }

    private val _partnerLoginState = MutableLiveData<PartnerLoginState>(PartnerLoginState.Init)
    val partnerLoginState: LiveData<PartnerLoginState> = _partnerLoginState

    private val _effect = LiveEvent<PartnerLoginState.Effect?>()
    val effect: LiveData<PartnerLoginState.Effect?> = _effect

    fun validatePartnerId(partnerId: String) {
        updateState(PartnerLoginState.Loading)

        if (partnerId.equals(partnerInfoId.value)) {
            // just continue on if the value is the same
            updateState(PartnerLoginState.Init)
            updateEffect(PartnerLoginState.Success)

        } else {
            viewModelScope.launch(dispatchers.io) {
                runCatching {
                    rockyService.getPartnerName(partnerId.toString()).toBlocking().value()

                }.onSuccess {
                    if (it.response()!!.isSuccessful) {
                        val response = it.response()!!
                        val body = response.body()!!

                        partnerInfoDao.deleteAllPartnerInfo()
                        partnerInfoDao.insertPartnerInfo(PartnerInfo(
                                partnerId = partnerId,
                                appVersion = body.appVersion().toFloat(),
                                isValid = body.isValid,
                                partnerName = body.partnerName(),
                                registrationDeadlineDate = body.registrationDeadlineDate(),
                                registrationNotificationText = body.registrationNotificationText(),
                                volunteerText = body.partnerVolunteerText()
                        ))

                        updateState(PartnerLoginState.Init)
                        updateEffect(PartnerLoginState.Success)
                    } else {
                        updateState(PartnerLoginState.Init)
                        updateEffect(PartnerLoginState.Error)
                    }

                }.onFailure {
                    Timber.d("API request failure - partner validation")
                    updateState(PartnerLoginState.Init)
                    updateEffect(PartnerLoginState.Error)
                }
            }
        }

    }

    fun clearPartnerInfo() {
        Timber.d("Deleting partner info")
        viewModelScope.launch(dispatchers.io) {
            partnerInfoDao.deleteAllPartnerInfo()
        }
    }

    private fun updateState(newState: PartnerLoginState) {
        Timber.d("Handling new state: $newState")
        _partnerLoginState.postValue(newState)
    }

    private fun updateEffect(newEffect: PartnerLoginState.Effect) {
        Timber.d("Handling new effect: $newEffect")
        _effect.postValue(newEffect)
    }

}

class PartnerLoginViewModelFactory(
        private val rockyService: RockyService,
        private val partnerInfoDao: PartnerInfoDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val dispatchers = DispatcherProviderImpl()

        @Suppress("UNCHECKED_CAST")
        return PartnerLoginViewModel(dispatchers, rockyService, partnerInfoDao) as T
    }
}