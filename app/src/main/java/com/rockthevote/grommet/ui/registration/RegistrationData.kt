package com.rockthevote.grommet.ui.registration

import android.util.Base64
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
            voterRecordsRequest = buildVoterRecordsRequest(partnerInformation)
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
            voterRegistration = buildVoterRegistration()
        )
    }

    private fun buildVoterRegistration(): VoterRegistration {
        val mailingAddress = if (addressData.isMailingAddressDifferent) {
            addressData.mailingAddress ?: throw InvalidRegistrationException(
                "addressData.isMailingAddressDifferent is null",
                "Mailing address must be filled out if 'I get my mail from a different address from the one above' is checked. "
            )
        } else {
            null
        }

        val race = (additionalInfoData.race ?: Race.DECLINE).toString()
        val party = when (additionalInfoData.party) {
            Party.OTHER_PARTY -> additionalInfoData.otherPoliticalParty
                ?: throw InvalidRegistrationException(
                    "otherPoliticalParty is null",
                    "Other Political Party was selected, a write in party is required"
                )
            else -> additionalInfoData.party.toString()
        }

        val signature = Base64.encodeToString(reviewData.signature, Base64.DEFAULT)

        return VoterRegistration(
            registrationHelper = buildRegistrationHelper(),
            dateOfBirth = Dates.formatAsISO8601_Date(newRegistrationData.birthday),
            mailingAddress = mailingAddress?.toApiAddressData(),
            previousRegistrationAddress = addressData.previousAddress?.toApiAddressData(),
            registrationAddress = addressData.homeAddress.toApiAddressData(),
            registrationAddressIsMailingAddress = !addressData.isMailingAddressDifferent,
            name = newRegistrationData.name.toApiName(),
            previousName = newRegistrationData.previousName?.toApiName(),
            gender = Gender.fromPrefix(newRegistrationData.name.title).toString(),
            race = race, // TODO race is required in the API, but not in the app?
            party = party,
            voterClassifications = buildVoterClassifications(),
            signature = Signature(
                mimeType = "image/png",
                image = signature
            ),
            voterIds = buildVoterIds(),
            contactMethods = buildContactMethods(),
            additionalInfo = buildAdditionalInfo()
        )

    }

    private fun buildRegistrationHelper(): RegistrationHelper? {
        if (!assistanceData.hasSomeoneAssisted) return null

        val helperPhone = assistanceData.helperPhone ?: throw InvalidRegistrationException(
            "helperPhone is null",
            "Assistant's phone number must be filled out if 'Did someone help you with this form?' is checked."
        )

        return RegistrationHelper(
            registrationHelperType = "assistant", // TODO is this a default value?
            name = assistanceData.helperName?.toApiName(),
            address = assistanceData.helperAddress?.toApiAddressData(),
            contactMethods = listOf(
                ContactMethod("phone", helperPhone, listOf("voice")) // TODO should capabilties be more here?
            )
        )
    }

    private fun buildVoterClassifications(): List<VoterClassification> {
        val is18OnElectionDay = VoterClassification(
            type = "eighteen_on_election_day",
            assertion = newRegistrationData.is18OrOlderByNextElection
        )

        val isUsCitizen = VoterClassification(
            type = "united_states_citizen",
            assertion = newRegistrationData.isUsCitizen
        )

        val sendCopyInEmail = VoterClassification(
            type = "send_copy_in_mail",
            assertion = additionalInfoData.hasOptedIntoNewsUpdates // TODO validate this is the correct field
        )

        val agreedToDeclaration = VoterClassification(
            type = "agreed_to_declaration",
            assertion = true // TODO I can't find where this comes from
        )

        return listOf(
            is18OnElectionDay,
            isUsCitizen,
            sendCopyInEmail,
            agreedToDeclaration
        )
    }

    private fun buildVoterIds(): List<VoterId> {
        val driversLicense = VoterId(
            type = "drivers_license",
            stringValue = additionalInfoData.pennDotNumber,
            attestNoSuchId = !additionalInfoData.knowsPennDotNumber
        )

        val ssn4 = VoterId(
            type = "ssn4",
            stringValue = additionalInfoData.ssnLastFour,
            attestNoSuchId = !additionalInfoData.knowsSsnLastFour
        )

        // TODO There's a enum value for state_id, but none in the app that I can find?

        return listOf(driversLicense, ssn4)
    }

    private fun buildContactMethods(): List<ContactMethod> {
        val phoneCapabilities = listOfNotNull(
            "voice",
            if (additionalInfoData.phoneType == PhoneType.MOBILE) "sms" else null
        )

        val phone = ContactMethod(
            type = "phone",
            value = additionalInfoData.phoneNumber,
            capabilities = phoneCapabilities
        )

        val email = ContactMethod(
            type = "email",
            value = additionalInfoData.emailAddress,
            capabilities = null
        )

        return listOf(phone, email)
    }

    private fun buildAdditionalInfo(): List<AdditionalInfo> {
        val preferredLanguage = AdditionalInfo(
            name = "preferred_language",
            stringValue = additionalInfoData.preferredLanguage?.toString()
        )

        val assistantDeclaration = AdditionalInfo(
            name = "assistant_declaration",
            stringValue = if (assistanceData.hasSomeoneAssisted) {
                if (assistanceData.hasConfirmedTerms) {
                    "true"
                } else {
                    throw InvalidRegistrationException(
                        "assistanceData.hasSomeoneAssisted is true but, did not confirm terms",
                        "'Did someone help you with this form?' is checked, but the "
                    )
                }
            } else {
                "false"
            }
        )

        return listOf(preferredLanguage, assistantDeclaration)
    }

    private fun PersonNameData.toApiName(): Name {
        return Name(
            firstName = firstName,
            lastName = lastName,
            middleName = middleName,
            titlePrefix = title.toString(), // TODO should this be localized?
            titleSuffix = suffix?.toString() // TODO should this be localized?
        )
    }

    private fun AddressData.toApiAddressData(): Address {
        val base = NumberedThoroughfareAddress(
            completeAddressNumber = null, // TODO from docs, is this anything different?
            completeStreetName = streetAddress,
            completeSubAddress = buildSubAddress(),
            completePlaceNames = buildCompletePlacesNames(),
            state = state,
            zipCode = zipCode
        )

        return Address(base)
    }

    // TODO How is this formatted currently?
    private fun AddressData.buildSubAddress(): List<CompleteSubAddress>? {
        val subAddres =  CompleteSubAddress(
            subAddressType = unitType,
            subAddress = listOfNotNull(unitType, unitNumber).joinToString(" ")
        )

        return listOf(subAddres)
    }

    private fun AddressData.buildCompletePlacesNames(): List<CompletePlaceName> {
        val city = CompletePlaceName("MunicipalJurisdiction", city)
        val county = CompletePlaceName("County", county) // TODO does county need "County" prepended to match docs?

        return listOf(city, county)
    }

    private fun validateRegistrationData(registrationData: RegistrationData) {
        // TODO localize all of this?
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