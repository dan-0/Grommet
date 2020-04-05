package com.rockthevote.grommet.ui.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rockthevote.grommet.data.db.model.GeoLocation
import com.rockthevote.grommet.data.db.model.RockyRequest
import com.rockthevote.grommet.ui.registration.address.PersonalInfoData
import com.rockthevote.grommet.ui.registration.assistance.AssistanceData
import com.rockthevote.grommet.ui.registration.name.NewRegistrantData
import com.rockthevote.grommet.ui.registration.personal.AdditionalInfoData
import com.rockthevote.grommet.ui.registration.review.ReviewData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import timber.log.Timber

class RegistrationViewModel(
    // TODO Remove default args before merge
    private val partnerInformation: PartnerInformation = PartnerInformation(1, "temp"),
    // TODO this should not make it into merge, it's a placeholder for data I don't know the origin of yet
    private val unknownDataSource: UnknownDataSource = UnknownDataSource(true, true, true, "temp", "temp", GeoLocation(1.0, 1.0), "temp")
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
            val transformer = RegistrationDataTransformer(currentData, partnerInformation)
            val requestData = transformer.transform(unknownDataSource)

            /* TODO Either inject an adapter, make this an asnc operation, or just pass the data
                object to whatever class handles storing this to offload the responsibility
             */
            val adapter = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
                .adapter(RockyRequest::class.java)

            val rockyRequestJson = adapter.toJson(requestData)

            Timber.d("Storing RockyRequest JSON %s", rockyRequestJson)

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

