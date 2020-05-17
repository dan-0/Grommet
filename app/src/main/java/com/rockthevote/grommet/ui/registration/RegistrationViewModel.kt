package com.rockthevote.grommet.ui.registration

import androidx.lifecycle.*
import com.rockthevote.grommet.data.db.dao.RegistrationDao
import com.rockthevote.grommet.data.db.dao.SessionDao
import com.rockthevote.grommet.data.db.model.GeoLocation
import com.rockthevote.grommet.data.db.model.Registration
import com.rockthevote.grommet.data.db.model.RockyRequest
import com.rockthevote.grommet.ui.registration.address.PersonalInfoData
import com.rockthevote.grommet.ui.registration.assistance.AssistanceData
import com.rockthevote.grommet.ui.registration.name.NewRegistrantData
import com.rockthevote.grommet.ui.registration.personal.AdditionalInfoData
import com.rockthevote.grommet.ui.registration.review.ReviewAndConfirmState
import com.rockthevote.grommet.ui.registration.review.ReviewData
import com.rockthevote.grommet.util.coroutines.DispatcherProvider
import com.rockthevote.grommet.util.coroutines.DispatcherProviderImpl
import com.rockthevote.grommet.util.extensions.toReviewAndConfirmStateData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class RegistrationViewModel(
    private val registrationDao: RegistrationDao,
    private val dispatcherProvider: DispatcherProvider = DispatcherProviderImpl(),
    private val sessionDao: SessionDao
) : ViewModel() {

    private val _registrationData = MutableLiveData(RegistrationData())
    val registrationData: LiveData<RegistrationData> = _registrationData

    private val _registrationState: MutableLiveData<RegistrationState> = MutableLiveData(RegistrationState.Init)
    val registrationState: LiveData<RegistrationState> = _registrationState

    private val _reviewAndConfirmState = MediatorLiveData<ReviewAndConfirmState>().apply {
        addSource(registrationData) {
            val data = it.toReviewAndConfirmStateData()
            postValue(ReviewAndConfirmState.Content(data))
        }
    }
    val reviewAndConfirmState: LiveData<ReviewAndConfirmState> = _reviewAndConfirmState

    private val currentRegistrationData
        get() = _registrationData.value ?: RegistrationData()

    fun storeNewRegistrantData(data: NewRegistrantData) {
        val newData = currentRegistrationData.copy(
            newRegistrantData = data
        )

        updateData(newData)
    }

    fun storeAddressData(data: PersonalInfoData) {
        val newData = currentRegistrationData.copy(
            addressData = data
        )

        updateData(newData)
    }

    fun storeAdditionalInfoData(data: AdditionalInfoData) {
        val newData = currentRegistrationData.copy(
            additionalInfoData = data
        )

        updateData(newData)
    }

    fun storeAssistanceData(data: AssistanceData) {
        val newData = currentRegistrationData.copy(
            assistanceData = data
        )

        updateData(newData)
    }

    private fun storeReviewData(data: ReviewData) {
        val newData = currentRegistrationData.copy(
            reviewData = data
        )

        updateData(newData, true)
    }

    @JvmOverloads
    fun completeRegistration(
        data: ReviewData,
        completionDate: Date = Date()
    ) {
        storeReviewData(data)

        viewModelScope.launch(dispatcherProvider.io) {

            val currentSession = sessionDao.getCurrentSession()

            val sessionData = currentSession?.let {
                SessionData(
                    partnerId = currentSession.partnerInfoId,
                    canvasserName = currentSession.canvasserName,
                    sourceTrackingId = currentSession.sourceTrackingId,
                    partnerTrackingId = currentSession.partnerTrackingId,
                    geoLocation = GeoLocation(
                        lat = currentSession.geoLocation.latitude(),
                        long = currentSession.geoLocation.longitude()
                    ),
                    openTrackingId = currentSession.openTrackingId
                )
            } ?: SessionData(
                partnerId = -1,
                canvasserName = "empty",
                sourceTrackingId = "empty",
                partnerTrackingId = "empty",
                geoLocation = GeoLocation(-1.0, -1.0),
                openTrackingId = "empty"
            ) // TODO this is default if a session doesn't exist, probably good to keep it just in case?

            runCatching {
                val transformer = RegistrationDataTransformer(currentRegistrationData, sessionData, completionDate)
                val requestData = transformer.transform()

                val adapter = Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()
                    .adapter(RockyRequest::class.java)

                val rockyRequestJson = adapter.toJson(requestData)

                val registration = Registration(
                    registrationData = rockyRequestJson
                )

                registrationDao.insert(registration)
                // TODO Send request data to DB
            }.onSuccess {
                updateState(RegistrationState.Complete)
            }.onFailure {
                Timber.e(it)
                when (it) {
                    is InvalidRegistrationException -> {
                        val newState = RegistrationState.RegistrationError(
                            isAcknowledged = false,
                            errorMsg = it.userMessage,
                            formatVar = it.formatVar
                        )

                        updateState(newState)
                    }
                    // We don't expect other exceptions, so throw it
                    else -> throw it
                }
            }
        }
    }

    /**
     * Sets the state to InProgress if not already.
     */
    private fun setStateToInProgress() {
        if (_registrationState.value != RegistrationState.InProgress) {
            updateState(RegistrationState.InProgress)
        }
    }

    private fun updateData(data: RegistrationData, synchronousUpdate: Boolean = false) {
        Timber.d("Updating registration data: %s", data)

        setStateToInProgress()

        if (!synchronousUpdate) {
            _registrationData.postValue(data)
        } else {
            _registrationData.value = data
        }
    }

    private fun updateState(state: RegistrationState) {
        Timber.d("Updating registration state: %s", state)

        _registrationState.postValue(state)
    }
}

class RegistrationViewModelFactory(
    private val registrationDao: RegistrationDao,
    private val sessionDao: SessionDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RegistrationViewModel(
            registrationDao = registrationDao,
            sessionDao = sessionDao) as T
    }
}

