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
        result?.let {
            CanvasserInfoData(
                    result.partnerInfo?.partnerInfoId ?: 0,
                    result.partnerInfo?.partnerName ?: "",
                    result.session?.canvasserName ?: "",
                    result.session?.openTrackingId ?: "",
                    result.session?.partnerTrackingId ?: "",
                    result.session?.deviceId ?: ""
            )
        } ?: run {
            CanvasserInfoData()
        }
    }

    private val _effect = LiveEvent<CanvasserInfoState.Effect?>()
    val effect: LiveData<CanvasserInfoState.Effect?> = _effect

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
    }

    fun updateCanvasserInfo(canvasserName: String,
                            partnerTrackingId: String,
                            openTrackingId: String,
                            deviceId: String) {


        viewModelScope.launch(dispatchers.io + coroutineExceptionHandler) {

            val data = canvasserInfoData.value ?: CanvasserInfoData()

            if (canvasserName == data.canvasserName
                    && partnerTrackingId == data.partnerTrackingId
                    && openTrackingId == data.openTrackingId
                    && deviceId == data.deviceId
            ) {
                // nothing changed, don't update the database, just continue
                updateEffect(CanvasserInfoState.Success)
            } else {
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