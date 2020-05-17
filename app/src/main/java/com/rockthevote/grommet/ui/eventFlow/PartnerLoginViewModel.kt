package com.rockthevote.grommet.ui.eventFlow

import androidx.lifecycle.*
import com.rockthevote.grommet.data.api.RockyService
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao
import com.rockthevote.grommet.data.db.model.PartnerInfo
import com.rockthevote.grommet.util.coroutines.DispatcherProvider
import com.rockthevote.grommet.util.coroutines.DispatcherProviderImpl
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Created by Mechanical Man on 5/16/20.
 */
class PartnerLoginViewModel(
        dispatchers: DispatcherProvider = DispatcherProviderImpl(),
        private val rockyService: RockyService,
        private val partnerInfoDao: PartnerInfoDao
) : ViewModel() {

    val partnerInfoId: LiveData<Long> =
            Transformations.map(partnerInfoDao.getCurrentPartnerInfo()) { result ->
                result.partnerInfoId
            }

    private val _partnerLoginState = MutableLiveData<PartnerLoginState>(PartnerLoginState.Init)
    val partnerLoginState: LiveData<PartnerLoginState> = _partnerLoginState

    private val _effect = MutableLiveData<PartnerLoginState.Effect?>()
    val effect: LiveData<PartnerLoginState.Effect?> = _effect

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        // TODO Should we handle this another way? Is it needed?
        Timber.e(throwable)
    }

    private val rockyRequestScope = CoroutineScope(dispatchers.io + coroutineExceptionHandler)

    init {
        viewModelScope.launch(dispatchers.io) {}
    }

    fun validatePartnerId(partnerId: Long) {
        updateState(PartnerLoginState.Loading)

        if (partnerId == partnerInfoId.value) {
            // just continue on if the value is the same
            updateState(PartnerLoginState.Init)
            updateEffect(PartnerLoginState.Success)

        } else {
            rockyRequestScope.launch {
                runCatching {
                    rockyService.getPartnerName(partnerId.toString())
                }.onSuccess {
                    if (it.isSuccessful) {
                        partnerInfoDao.insertPartnerInfo(PartnerInfo(
                                partnerId,
                                it.body()!!.appVersion().toFloat(),
                                it.body()!!.isValid,
                                it.body()!!.partnerName(),
                                it.body()!!.registrationDeadlineDate(),
                                it.body()!!.registrationNotificationText(),
                                it.body()!!.partnerVolunteerText()
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
        partnerInfoDao.deletePartnerInfo()
    }

    private fun updateState(newState: PartnerLoginState) {
        Timber.d("Handling new state: $newState")
        _partnerLoginState.postValue(newState)
    }

    private fun updateEffect(newEffect: PartnerLoginState.Effect) {
        Timber.d("Handling new effect: $newEffect")
        _effect.postValue(newEffect)

        // clear effect
        _effect.postValue(null)
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