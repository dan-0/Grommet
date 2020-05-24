package com.rockthevote.grommet.ui

import androidx.lifecycle.*
import com.rockthevote.grommet.data.api.RockyService
import com.rockthevote.grommet.data.api.model.RegistrationResponse
import com.rockthevote.grommet.data.db.dao.RegistrationDao
import com.rockthevote.grommet.data.db.model.RockyRequest
import com.rockthevote.grommet.util.coroutines.DispatcherProvider
import com.rockthevote.grommet.util.coroutines.DispatcherProviderImpl
import kotlinx.coroutines.*
import retrofit2.adapter.rxjava.Result
import rx.Observable
import timber.log.Timber

class MainActivityViewModel(
    private val dispatchers: DispatcherProvider = DispatcherProviderImpl(),
    private val rockyService: RockyService,
    private val registrationDao: RegistrationDao
) : ViewModel() {

    private val _state = MutableLiveData<MainActivityState>(MainActivityState.Init)
    val state: LiveData<MainActivityState> = _state

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        // TODO Should we handle this another way? Is it needed?
        Timber.e(throwable)
    }

    private val supervisorJob = SupervisorJob()

    private val rockyRequestScope = CoroutineScope(dispatchers.io + coroutineExceptionHandler)

    init {
        viewModelScope.launch(dispatchers.io) {
            updateState(MainActivityState.Loading)

            val requests = loadRequestsFromDb()

            val content = MainActivityState.Content(
                pendingUploads = requests.size,
                failedUploads = 0 // TODO need to figure out how we're going to calculate this
            )

            updateState(content)
        }
    }

    fun uploadRegistrations() {
        updateState(MainActivityState.Loading)

        rockyRequestScope.launch {
            // TODO get all requests from database
            val requests = loadRequestsFromDb()

            val resultMap = mutableMapOf<RockyRequest, Observable<Result<RegistrationResponse>>>()

            requests.forEach {
                rockyRequestScope.launch {
                    runCatching {
                        val result = rockyService.register(it)
                        resultMap[it] = result
                        // TODO Error handling for runCatching
                    }
                }
            }

            resultMap.forEach {
                // TODO Handle observable results
            }

            // TODO populate this with real values
            val result = MainActivityState.Content(
                pendingUploads = 1,
                failedUploads = 2
            )

            updateState(result)
        }
    }

    private suspend fun loadRequestsFromDb(): List<RockyRequest> {
        // TODO implement real functionality
        return listOf()
    }

    /**
     * Asynchronously determines if the user can clock out. Calls [successCallback]
     * when the user can logout, [failCallback] when the user cannot.
     *
     * Note: Coroutines cannot be used from Java, so callbacks are necessary
     */
    fun asyncCanClockOut(successCallback: () -> Unit, failCallback: () -> Unit) {
        viewModelScope.launch(dispatchers.io) {
            val canClockOut = loadRequestsFromDb().isEmpty()

            withContext(dispatchers.main) {
                if (canClockOut) {
                    successCallback()
                } else {
                    failCallback()
                }
            }
        }
    }

    private fun updateState(newState: MainActivityState) {
        Timber.d("Handling new state: $newState")
        _state.postValue(newState)
    }

    override fun onCleared() {
        super.onCleared()
        supervisorJob.cancelChildren()
    }
}

class MainActivityViewModelFactory(
    private val rockyService: RockyService,
    private val registrationDao: RegistrationDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val dispatchers = DispatcherProviderImpl()

        @Suppress("UNCHECKED_CAST")
        return MainActivityViewModel(dispatchers, rockyService, registrationDao) as T
    }
}