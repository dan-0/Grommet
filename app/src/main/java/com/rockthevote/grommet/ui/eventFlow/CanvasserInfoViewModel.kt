package com.rockthevote.grommet.ui.eventFlow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rockthevote.grommet.data.api.RockyService
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