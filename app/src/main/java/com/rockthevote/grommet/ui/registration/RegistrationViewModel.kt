package com.rockthevote.grommet.ui.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rockthevote.grommet.ui.registration.address.PersonalInfoData
import com.rockthevote.grommet.ui.registration.assistance.AssistanceData
import com.rockthevote.grommet.ui.registration.name.NewRegistrantData
import com.rockthevote.grommet.ui.registration.personal.AdditionalInfoData
import com.rockthevote.grommet.ui.registration.review.ReviewData
import com.squareup.moshi.Moshi
import timber.log.Timber

class RegistrationViewModel(
    // TODO Make all params non-null before merge
    private val partnerInformation: PartnerInformation? = null,
    // TODO this should not make it into merge, it's a placeholder for data I don't know the origin of yet
    private val unknownDataSource: UnknownDataSource? = null
) : ViewModel() {
    private val _registrationData = MutableLiveData(RegistrationData())
    val registrationData: LiveData<RegistrationData> = _registrationData

    private val _registrationState: MutableLiveData<RegistrationState> = MutableLiveData(RegistrationState.Init)
    private val registrationState: LiveData<RegistrationState> = _registrationState

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

        updateData(newData)
    }

    fun completeRegistration(data: ReviewData) {
        storeReviewData(data)

        runCatching {
            val transformer = RegistrationDataTransformer(currentData, partnerInformation!!)
            val requestData = transformer.transform(unknownDataSource!!)

            // TODO Send request data to DB
        }.onSuccess {
            _registrationState.postValue(RegistrationState.Complete)
        }.onFailure {
            TODO("Handle the failure, send RegistrationError")
        }

    }

    private fun updateData(data: RegistrationData) {
        Timber.d("Updating registration data: %s", data)

        setStateToInProgress()
        _registrationData.postValue(data)
    }

    /**
     * Sets the state to InProgress if not already.
     */
    private fun setStateToInProgress() {
        if (_registrationState.value != RegistrationState.InProgress) {
            _registrationState.postValue(RegistrationState.InProgress)
        }
    }
}

