package com.rockthevote.grommet.ui.registration

import android.util.Base64
import com.rockthevote.grommet.data.db.model.*
import com.rockthevote.grommet.ui.registration.address.AddressData
import com.rockthevote.grommet.ui.registration.name.PersonNameData
import com.rockthevote.grommet.util.Dates
import java.util.*

/**
 * Handles transformation of [registrationData]
 */
class RegistrationDataTransformer @Throws(InvalidRegistrationException::class) constructor(
    private val registrationData: RegistrationData,
    private val partnerInformation: PartnerInformation
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
     * Transforms the [registrationData] and [partnerInformation] into a [RockyRequest]
     *
     * @throws [InvalidRegistrationException] if unable to validate [RegistrationData]
     */
    fun transform(
        unknown: UnknownDataSource
    ): RockyRequest {

        val body = RockyRequestBody(
            lang = getLanguageCompletedIn(),
            phoneType = null, // TODO I can't find where/what is supposed to populate this
            partnerId = partnerInformation.partnerId,
            optInEmail = additionalInfoData.hasOptedIntoNewsUpdates,
            optInSms = additionalInfoData.hasOptedIntoNewsCallAndText,
            optInVolunteer = additionalInfoData.hasOptedForVolunteerText,
            // TODO All of these data sources are unknown, I think they may be from partner information, but don't know for sure
            partnerOptInSms = unknown.partnerOptInSms,
            partnerOptInEmail = unknown.partnerOptInEmail,
            partnerOptInVolunteer = unknown.partnerOptInVolunteer,
            finishWithState = true, // TODO is this a default value?
            createdViaApi = true, // TODO is this a default value?
            sourceTrackingId = unknown.sourceTrackingId,
            partnerTrackingId = unknown.partnerTrackingId,
            geoLocation = unknown.geoLocation,
            openTrackingId = unknown.openTrackingId,
            voterRecordsRequest = buildVoterRecordsRequest()
        )

        return RockyRequest(body)
    }

    private fun getLanguageCompletedIn(): String {
        val spanish = FormLanguage.SPANISH.toString()

        return if (Locale.getDefault().language == spanish) {
            spanish
        } else {
            FormLanguage.ENGLISH.toString()
        }
    }

    private fun buildVoterRecordsRequest(): VoterRecordsRequest {
        return VoterRecordsRequest(
            type = "registration", // TODO is this a default value?
            generatedDate = Dates.formatAsISO8601_Date(Date()), // TODO is this from somewhere else, I can't find a source?
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

        // TODO Is this the way that we should be specifying race? It's required in the API, but not in the app
        val race = (additionalInfoData.race ?: Race.DECLINE).toString()

        val party = when (additionalInfoData.party) {
            Party.OTHER_PARTY -> additionalInfoData.otherPoliticalParty
                ?: throw InvalidRegistrationException(
                    "otherPoliticalParty is null",
                    "Other Political Party was selected, a write in party is required"
                )
            else -> additionalInfoData.party.toString()
        }

        // TODO NO_WRAP was specified in ApiSignature, but probably should be in the API docs as well
        val signature = Base64.encodeToString(reviewData.signature, Base64.NO_WRAP)

        return VoterRegistration(
            registrationHelper = buildRegistrationHelper(),
            dateOfBirth = Dates.formatAsISO8601_ShortDate(newRegistrationData.birthday), //TODO DateAdapter used short date, is this correct?
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
        if (!assistanceData.hasSomeoneAssisted) return null // TODO is this correct, or should this be based on the canvasser and always non-null?

        val helperPhone = assistanceData.helperPhone ?: throw InvalidRegistrationException(
            "helperPhone is null",
            "Assistant's phone number must be filled out if 'Did someone help you with this form?' is checked."
        )

        return RegistrationHelper(
            registrationHelperType = "assistant", // TODO is this a default value?
            name = assistanceData.helperName?.toApiName(),
            address = assistanceData.helperAddress?.toApiAddressData(),
            contactMethods = listOf(
                ContactMethod(
                    type = "phone",
                    value = helperPhone,
                    capabilities = listOf("voice") // TODO should capabilties be more here? IIRC, this is only VOICE from [ApiContactMethod]
                )
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
            type = "send_copy_in_mail", // TODO Is this "I give permission to OSET Org to send me news and updates?"
            assertion = additionalInfoData.hasOptedIntoNewsUpdates
        )

        val agreedToDeclaration = VoterClassification(
            type = "agreed_to_declaration", // TODO I can't find where this comes from, but it's in the API docs
            assertion = true // TODO Defaulting to this until source is confirmed
        )

        // TODO, this isn't in the docs, but is in the old VoterClassification, should it be?
        val politicalPartyChanged = VoterClassification(
            type = "political_party_change",
            assertion = additionalInfoData.hasChangedPoliticalParty
        )

        return listOfNotNull(
            is18OnElectionDay,
            isUsCitizen,
            sendCopyInEmail,
            agreedToDeclaration,
            politicalPartyChanged
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

        // TODO There's a enum value for state_id, but none in the app that I can find. What is the source? Should it be here? It isn't in the old VoterId type

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
            completeAddressNumber = null,
            completeStreetName = streetAddress,
            completeSubAddress = buildSubAddress(),
            completePlaceNames = buildCompletePlacesNames(),
            state = state,
            zipCode = zipCode
        )

        return Address(base)
    }

    private fun AddressData.buildSubAddress(): List<CompleteSubAddress>? {
        val unitType = unitType?.let {
            CompleteSubAddress(
                subAddressType = it,
                subAddress = listOfNotNull(unitType, unitNumber).joinToString(" ")
            )
        }

        val line2 = streetAddressTwo?.let {
            CompleteSubAddress(
                subAddressType = "LINE2",
                subAddress = it
            )
        }

        return listOfNotNull(unitType, line2)
    }

    private fun AddressData.buildCompletePlacesNames(): List<CompletePlaceName> {
        val city = CompletePlaceName("MunicipalJurisdiction", city)
        val county = CompletePlaceName("County", county) // TODO Does county need "County" appended to match docs, eg ("Allegheny County")?

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