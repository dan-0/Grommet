package com.rockthevote.grommet.ui.registration

import androidx.lifecycle.*
import com.rockthevote.grommet.data.db.dao.RegistrationDao
import com.rockthevote.grommet.data.db.model.GeoLocation
import com.rockthevote.grommet.data.db.model.Registration
import com.rockthevote.grommet.data.db.model.RockyRequest
import com.rockthevote.grommet.ui.registration.address.PersonalInfoData
import com.rockthevote.grommet.ui.registration.assistance.AssistanceData
import com.rockthevote.grommet.ui.registration.name.NewRegistrantData
import com.rockthevote.grommet.ui.registration.personal.AdditionalInfoData
import com.rockthevote.grommet.ui.registration.review.ReviewData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class RegistrationViewModel(
    // TODO this is temporary until we can provide this from the session database
    private val sessionData: SessionData = SessionData(
        1,
        "temp",
        "temp",
        "temp",
        GeoLocation(1.0, 1.0),
        "temp"
    ),
    private val registrationDao: RegistrationDao
) : ViewModel() {
    private val _registrationData = MutableLiveData(RegistrationData())
    val registrationData: LiveData<RegistrationData> = _registrationData

    private val _registrationState: MutableLiveData<RegistrationState> = MutableLiveData(RegistrationState.Init)
    val registrationState: LiveData<RegistrationState> = _registrationState

    private val currentData
        get() = _registrationData.value ?: RegistrationData()

    fun storeNewRegistrantData(data: NewRegistrantData) {
        val newData = currentData.copy(
            newRegistrantData = data
        )

        updateData(newData)
    }

    fun storeAddressData(data: PersonalInfoData) {
        val newData = currentData.copy(
            addressData = data
        )

        updateData(newData)
    }

    fun storeAdditionalInfoData(data: AdditionalInfoData) {
        val newData = currentData.copy(
            additionalInfoData = data
        )

        updateData(newData)
    }

    fun storeAssistanceData(data: AssistanceData) {
        val newData = currentData.copy(
            assistanceData = data
        )

        updateData(newData)
    }

    private fun storeReviewData(data: ReviewData) {
        val newData = currentData.copy(
            reviewData = data
        )

        updateData(newData, true)
    }

    fun completeRegistration(data: ReviewData) {
        storeReviewData(data)

        runCatching {
            val transformer = RegistrationDataTransformer(currentData, sessionData)
            val requestData = transformer.transform()

            /* TODO Either inject an adapter, make this an async operation, or just pass the data
                object to whatever class handles storing this to offload the responsibility
             */
            val adapter = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
                .adapter(RockyRequest::class.java)

            val rockyRequestJson = adapter.toJson(requestData)

            Timber.d("Storing RockyRequest JSON %s", rockyRequestJson)

            val registrantName = with(currentData.newRegistrantData!!.name) {
                listOfNotNull(firstName, middleName, lastName).joinToString(" ")
            }

            val registration = Registration(
                    //TODO add session ID
                registrationDate = Date().time ,
                registrantName = registrantName,
                // This should only used if there was already an error on registration
                //  so allowing an empty email is acceptable in this case
                registrantEmail = currentData.additionalInfoData?.emailAddress ?: "",
                registrationData = rockyRequestJson
            )

            viewModelScope.launch(Dispatchers.IO) {
                registrationDao.insert(registration)
            }


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
    private val registrationDao: RegistrationDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RegistrationViewModel(registrationDao = registrationDao) as T
    }
}