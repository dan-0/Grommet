package com.rockthevote.grommet.ui

import androidx.lifecycle.*
import com.rockthevote.grommet.data.db.dao.SessionDao
import com.rockthevote.grommet.data.db.model.Session
import com.rockthevote.grommet.util.coroutines.DispatcherProvider
import kotlinx.coroutines.launch
import timber.log.Timber

class SessionViewModel(
    private val sessionDao: SessionDao,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _state = MutableLiveData<SessionStartState>(SessionStartState.Init)
    val state: LiveData<SessionStartState> = _state

    fun updatePartnerId(newId: String) {

        val newState = when (val currentState = state.value!!) {
            SessionStartState.Init -> {
                val data = SessionInitializationData(
                    partnerId = newId
                )
                SessionStartState.InProgress(data)
            }

            is SessionStartState.Content -> {
                val data = currentState.data.copy(
                    partnerId = newId
                )
                SessionStartState.InProgress(data)
            }
        }

        updateState(newState)
    }

    fun startSession(
        canvasserName: String,
        location: String,
        zipCode: String,
        tablet: String
    ) {
        val newState = when (val currentState = state.value!!) {
            is SessionStartState.Content -> {
                val data = currentState.data.copy(
                    canvasserName = canvasserName,
                    location = location,
                    zipCode = zipCode,
                    deviceId = tablet
                )
                SessionStartState.Complete(data)
            }

            else -> throw IllegalStateException("Attempted to start session in invalid state: ${currentState::class.simpleName}")
        }

        updateState(newState)
    }

    private fun buildSession(data: SessionInitializationData) {
        TODO("Make Session object from SessionInitialization data and call updateSession here")
    }

    private suspend fun updateSession(session: Session) {
        viewModelScope.launch(dispatchers.io) {
            sessionDao.destroyTable()
            sessionDao.addSession(session)
        }
    }

    private fun updateState(newState: SessionStartState) {
        Timber.d("New state: ${newState::class.simpleName}")
        _state.postValue(newState)
    }
}

class SessionViewModelFactory(
    private val sessionDao: SessionDao,
    private val dispatchers: DispatcherProvider
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SessionViewModel(sessionDao, dispatchers) as T
    }
}

sealed class SessionStartState {
    object Init : SessionStartState()

    abstract class Content(
        val data: SessionInitializationData
    ) : SessionStartState()

    class InProgress(data: SessionInitializationData) : Content(data)

    class Complete(data: SessionInitializationData) : Content(data)
}

data class SessionInitializationData(
    val partnerId: String,
    val canvasserName: String? = null,
    val location: String? = null,
    val zipCode: String? = null,
    val deviceId: String? = null
)