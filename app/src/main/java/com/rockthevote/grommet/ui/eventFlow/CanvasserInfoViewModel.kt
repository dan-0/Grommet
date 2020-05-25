package com.rockthevote.grommet.ui.eventFlow

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hadilq.liveevent.LiveEvent
import com.rockthevote.grommet.data.db.dao.SessionDao
import com.rockthevote.grommet.util.coroutines.DispatcherProvider
import com.rockthevote.grommet.util.coroutines.DispatcherProviderImpl

/**
 * Created by Mechanical Man on 5/25/20.
 */

class CanvasserInfoViewModel(
        private val dispatchers: DispatcherProvider = DispatcherProviderImpl(),
        private val sessionDao: SessionDao
) : ViewModel() {

    lateinit var canvasserInfoData: LiveData<CanvasserInfoData>

    private val _effect = LiveEvent<CanvasserInfoState.Effect?>()
    val effect: LiveData<CanvasserInfoState.Effect?> = _effect

    init {
        Transformations.map(sessionDao.getSessionWithPartnerInfo()) { result ->
            CanvasserInfoData(
                    result.partnerInfo.partnerName,
                    result.session?.canvasserName ?: "",
                    result.session?.openTrackingId ?: "",
                    result.session?.partnerTrackingId ?: "",
                    result.session?.deviceId ?: ""
            )
        }
    }


    fun updateCanvasserInfo(canvasserInfoData: CanvasserInfoData) {
        //TODO create source tracking ID here and pass it in
        sessionDao.

    }
}

class CanvasserInfoViewModelFactory(
        private val sessionDao: SessionDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val dispatchers = DispatcherProviderImpl()

        @Suppress("UNCHECKED_CAST")
        return CanvasserInfoViewModel(dispatchers, sessionDao) as T
    }
}