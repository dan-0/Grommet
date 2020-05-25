package com.rockthevote.grommet.ui.eventFlow

import android.location.Location
import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import com.rockthevote.grommet.data.api.model.ApiGeoLocation
import com.rockthevote.grommet.data.db.dao.SessionDao
import com.rockthevote.grommet.data.db.model.Session
import com.rockthevote.grommet.util.coroutines.DispatcherProvider
import com.rockthevote.grommet.util.coroutines.DispatcherProviderImpl
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import pl.charmas.android.reactivelocation.ReactiveLocationProvider
import timber.log.Timber
import java.util.*

/**
 * Created by Mechanical Man on 5/25/20.
 */

class CanvasserInfoViewModel(
        private val dispatchers: DispatcherProvider = DispatcherProviderImpl(),
        private val sessionDao: SessionDao,
        private val reactiveLocationProvider: ReactiveLocationProvider
) : ViewModel() {

    val canvasserInfoData = Transformations.map(sessionDao.getSessionWithPartnerInfo()) { result ->
        CanvasserInfoData(
                result.partnerInfo.partnerInfoId,
                result.partnerInfo.partnerName,
                result.session?.canvasserName ?: "",
                result.session?.openTrackingId ?: "",
                result.session?.partnerTrackingId ?: "",
                result.session?.deviceId ?: ""
        )
    }

    private val _effect = LiveEvent<CanvasserInfoState.Effect?>()
    val effect: LiveData<CanvasserInfoState.Effect?> = _effect

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        // TODO Should we handle this another way? Is it needed?
        Timber.e(throwable)
    }

    fun updateCanvasserInfo(canvasserName: String,
                            partnerTrackingId: String,
                            openTrackingId: String,
                            deviceId: String) {


        viewModelScope.launch(dispatchers.io + coroutineExceptionHandler) {

            runCatching {
                val location: Location = reactiveLocationProvider.lastKnownLocation.toBlocking().first()

                sessionDao.clearAllSessionInfo()
                sessionDao.insert(Session(
                        partnerInfoId = canvasserInfoData.value?.partnerInfoId ?: 0,
                        canvasserName = canvasserName,
                        sourceTrackingId = canvasserName + Calendar.getInstance().timeInMillis,
                        partnerTrackingId = partnerTrackingId,
                        geoLocation = ApiGeoLocation.builder()
                                .latitude(location.latitude)
                                .longitude(location.longitude)
                                .build(),
                        openTrackingId = openTrackingId,
                        deviceId = deviceId
                )
                )
            }.onSuccess {
                updateEffect(CanvasserInfoState.Success)
            }.onFailure {
                Timber.d("failure updating canvasser info")
                updateEffect(CanvasserInfoState.Error)
            }

        }
    }

    private fun updateEffect(newEffect: CanvasserInfoState.Effect) {
        Timber.d("Handling new effect: $newEffect")
        _effect.postValue(newEffect)
    }
}

class CanvasserInfoViewModelFactory(
        private val sessionDao: SessionDao,
        private val reactiveLocationProvider: ReactiveLocationProvider
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val dispatchers = DispatcherProviderImpl()

        @Suppress("UNCHECKED_CAST")
        return CanvasserInfoViewModel(dispatchers, sessionDao, reactiveLocationProvider) as T
    }
}