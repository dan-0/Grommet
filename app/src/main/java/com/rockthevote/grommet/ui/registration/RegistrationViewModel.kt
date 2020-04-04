package com.rockthevote.grommet.ui.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rockthevote.grommet.data.db.model.GeoLocation
import com.rockthevote.grommet.data.db.model.VoterId
import com.rockthevote.grommet.ui.registration.address.PersonalInfoData
import com.rockthevote.grommet.ui.registration.assistance.AssistanceData
import com.rockthevote.grommet.ui.registration.name.NewRegistrantData
import com.rockthevote.grommet.ui.registration.personal.AdditionalInfoData
import com.rockthevote.grommet.ui.registration.review.ReviewData
import timber.log.Timber

class RegistrationViewModel : ViewModel() {
    private val _registrationData = MutableLiveData(RegistrationData())
    val registrationData: LiveData<RegistrationData> = _registrationData

    fun storeNewRegistrantData(data: NewRegistrantData) {
        val newData = _registrationData.value?.copy(
            newRegistrantData = data
        )

        updateData(newData)
    }

    fun storeAddressData(data: PersonalInfoData) {
        val newData = _registrationData.value?.copy(
            addressData = data
        )

        updateData(newData)
    }

    fun storeAdditionalInfoData(data: AdditionalInfoData) {
        val newData = _registrationData.value?.copy(
            additionalInfoData = data
        )

        updateData(newData)
    }

    fun storeAssistanceData(data: AssistanceData) {
        val newData = _registrationData.value?.copy(
            assistanceData = data
        )

        updateData(newData)
    }

    fun storeReviewData(data: ReviewData) {
        val newData = _registrationData.value?.copy(
            reviewData = data
        )

        updateData(newData)
    }

    private fun updateData(data: RegistrationData?) {
        Timber.d("Updating registration data: %s", data)
        data ?: return
        _registrationData.postValue(data)
    }
}

// TODO Find exactly what we need to provide so a request can be made from the VM
data class PartnerInformation(
    val partnerId: Int,
    val canvasserName: String
)

/**
 * TODO: I don't know where this data is derived from?
 * 
 * DO NOT MERGE THIS, it is a placeholder while I figure out where data comes from
 * for the API
 */
data class UnknownDataSource(
    val partnerOptInEmail: Boolean,
    val partnerOptInSms: Boolean,
    val partnerOptInVolunteer: Boolean,
    val sourceTrackingId: String,
    val partnerTrackingId: String,
    val geoLocation: GeoLocation,
    val openTrackingId: String
)
