package com.rockthevote.grommet.ui.registration

import com.rockthevote.grommet.data.db.model.*
import com.rockthevote.grommet.ui.registration.address.AddressData
import com.rockthevote.grommet.ui.registration.address.PersonalInfoData
import com.rockthevote.grommet.ui.registration.assistance.AssistanceData
import com.rockthevote.grommet.ui.registration.name.NewRegistrantData
import com.rockthevote.grommet.ui.registration.name.PersonNameData
import com.rockthevote.grommet.ui.registration.personal.AdditionalInfoData
import com.rockthevote.grommet.ui.registration.review.ReviewData
import com.rockthevote.grommet.util.Dates
import java.lang.Exception
import java.util.*

data class RegistrationData(
    val newRegistrantData: NewRegistrantData? = null,
    val addressData: PersonalInfoData? = null,
    val additionalInfoData: AdditionalInfoData? = null,
    val assistanceData: AssistanceData? = null,
    val reviewData: ReviewData? = null
)


class RegistrationDataTransformer @Throws(InvalidRegistrationException::class) constructor(
    private val registrationData: RegistrationData
) {

    init {
        validateRegistrationData(registrationData)
    }

    // These have already be evaluated as non null in [validateRegistrationData]
    private val newRegistrationData = registrationData.newRegistrantData!!
    private val addressData = registrationData.addressData!!
    private val additionalInfoData = registrationData.additionalInfoData!!
    private val assistanceData = registrationData.assistanceData!!
    private val reviewData = registrationData.reviewData!!

    /**
     * Transform [registrationData] and [partnerInformation] into a [RockyRequest]
     *
     * @throws [InvalidRegistrationException] if unable to validate [RegistrationData]
     */

    fun transform(
        partnerInformation: PartnerInformation,
        unknown: UnknownDataSource
    ): RockyRequest {
        // TODO [phone_type] is in top level attributes, but isn't present in the example?

        val body = RockyRequestBody(
            lang = getLanguageCompletedIn(),
            partnerId = partnerInformation.partnerId,
            optInEmail = additionalInfoData.hasOptedIntoNewsUpdates,
            optInSms = additionalInfoData.hasOptedIntoNewsCallAndText,
            optInVolunteer = additionalInfoData.hasOptedForVolunteerText,
            partnerOptInSms = unknown.partnerOptInSms,
            partnerOptInEmail = unknown.partnerOptInEmail,
            partnerOptInVolunteer = unknown.partnerOptInVolunteer,
            finishWithState = true, // TODO is this a default value?
            createdViaApi = true, // TODO is this a default value?
            sourceTrackingId = unknown.sourceTrackingId,
            partnerTrackingId = unknown.partnerTrackingId,
            geoLocation = unknown.geoLocation,
            openTrackingId = unknown.openTrackingId,
            voterRecordsRequest = null // TODO readd this: buildVoterRecordsRequest(registrationData)
        )

        return RockyRequest(body)
    }

    private fun getLanguageCompletedIn(): String {
        val spanish = RockyRequestLegacy.Language.SPANISH.toString()

        return if (Locale.getDefault().language == spanish) {
            spanish
        } else {
            RockyRequestLegacy.Language.ENGLISH.toString()
        }
    }

    private fun buildVoterRecordsRequest(
        partnerInformation: PartnerInformation
    ): VoterRecordsRequest {
        return VoterRecordsRequest(
            type = "registration", // TODO is this a default value?
            generatedDate = Dates.formatAsISO8601_Date(Date()), // TODO is this from somewhere else?
            canvasserName = partnerInformation.canvasserName,
            voterRegistration = null // TODO add this: buildVoterRegistration(registrationData)
        )
    }

    private fun buildVoterRegistration(): VoterRegistration {
        return VoterRegistration(
            registrationHelper = null // TODO readd this buildRegistrationHelper(registrationData)
        )
    }

    private fun buildRegistrationHelper(): RegistrationHelper? {
        return RegistrationHelper(
            registrationHelperType = "assistant", // TODO is this a default value?
            name = registrationData.assistanceData!!.helperName?.toApiName(),

        )
    }

    private fun PersonNameData.toApiName(): Name {
        return Name(
            firstName = firstName,
            lastName = lastName,
            middleName = middleName,
            titlePrefix = title.toString() // TODO should this be localized?
        )
    }

    private fun AddressData.toApiAddressData(): Address {
        val base = NumberedThoroughfareAddress(
            // TODO LEFT OFF HERE;laksdjfa
        )

        return Address(base)
    }

    private fun validateRegistrationData(registrationData: RegistrationData) {
        // TODO localize all of this
        val errorMsgTemplate = "%s is null"
        val userMessageTemplate = "Ensure all registrant data under '%s' is filled out completely."
        registrationData.newRegistrantData ?: throw InvalidRegistrationException(
            errorMsgTemplate.format("newRegistrantData"),
            userMessageTemplate.format("Name")
        )

        registrationData.addressData ?: throw InvalidRegistrationException(
            errorMsgTemplate.format("addressData"),
            userMessageTemplate.format("Address")
        )

        registrationData.additionalInfoData ?: throw InvalidRegistrationException(
            errorMsgTemplate.format("additionalInfoData"),
            userMessageTemplate.format("Personal")
        )

        registrationData.assistanceData ?: throw InvalidRegistrationException(
            errorMsgTemplate.format("assistanceData"),
            userMessageTemplate.format("Assistance")
        )

        registrationData.reviewData ?: throw InvalidRegistrationException(
            errorMsgTemplate.format("reviewData"),
            userMessageTemplate.format("Review")
        )
    }
}

/**
 * Provided registration data is invalid.
 */
class InvalidRegistrationException(
    msg: String,
    val userMessage: String
) : Exception(msg)