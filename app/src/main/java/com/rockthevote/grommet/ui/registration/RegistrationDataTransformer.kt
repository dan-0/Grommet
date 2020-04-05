package com.rockthevote.grommet.ui.registration

import android.util.Base64
import com.rockthevote.grommet.R
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
            lang = reviewData.formLanguage,
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

    private fun buildVoterRecordsRequest(): VoterRecordsRequest {
        return VoterRecordsRequest(
            type = "registration", // TODO is this a default value?
            // TODO is this from somewhere else, I can't find a source?
            // TODO Does this _have_ to be a full datetime stamp? It is in the example, but I don't see a formatter anywhere in the project
            //  Looks like not? From old RockyRequest.java: Date generatedDate = Dates.parseISO8601_Date(Db.getString(cursor, GENERATED_DATE));
            generatedDate = Dates.formatAsISO8601_Date(Date()),
            canvasserName = partnerInformation.canvasserName,
            voterRegistration = buildVoterRegistration()
        )
    }

    private fun buildVoterRegistration(): VoterRegistration {
        val mailingAddress = if (addressData.isMailingAddressDifferent) {
            addressData.mailingAddress ?: throw InvalidRegistrationException(
                "addressData.isMailingAddressDifferent is null",
                R.string.different_mailing_address_not_filled_out
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
                    R.string.must_write_in_party
                )
            else -> additionalInfoData.party.toString()
        }.toLowerCase() // TODO i

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
            R.string.assistants_phone_number_is_empty
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
            type = "send_copy_in_mail", // TODO Is this "I give permission to OSET Org to send me news and updates?" It's looking like it may not be, that might be "opt_in_email"? Where does this come from?
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
            // TODO JSON example has this split with spaces, but app doesn't format it, does that matter? eg 99999999 is 99 999 999 in JSON example
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
        // TODO Do we specify 'fax' anywhere? It's in the API example, but I can't see where we'd derive it from
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
            capabilities = listOf()
        )

        return listOf(phone, email)
    }

    private fun buildAdditionalInfo(): List<AdditionalInfo> {
        val preferredLanguage = AdditionalInfo(
            name = "preferred_language",
            stringValue = additionalInfoData.preferredLanguage?.toString()
        )

        // TODO confirming, is assistant_declaration based on "I CONFIRM THAT I HAVE... in assistant, or just "Did someone help you with this form"?
        val assistantDeclaration = AdditionalInfo(
            name = "assistant_declaration",
            stringValue = if (assistanceData.hasSomeoneAssisted) {
                if (assistanceData.hasConfirmedTerms) {
                    "true"
                } else {
                    throw InvalidRegistrationException(
                        "assistanceData.hasSomeoneAssisted is true but, did not confirm terms",
                        R.string.confirm_terms_not_checked
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
            completeAddressNumber = "",
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
            if (it.isEmpty()) return@let null

            val subAddressComponents = listOfNotNull(unitType, unitNumber)
                .nullIfEmpty() ?: return@let null

            CompleteSubAddress(
                subAddressType = it,
                // TODO Do we use the unitNumber as is, or append a transformed unitType like in the JSON example?
                subAddress = subAddressComponents.joinToString(" ")
            )
        }

        val line2 = streetAddressTwo?.let {
            if (it.isEmpty()) return@let null
            CompleteSubAddress(
                subAddressType = "LINE2",
                subAddress = it
            )
        }

        return listOfNotNull(unitType, line2).nullIfEmpty()
    }

    private fun AddressData.buildCompletePlacesNames(): List<CompletePlaceName>? {
        val city = CompletePlaceName("MunicipalJurisdiction", city)

        // TODO Does county need "County" appended to match docs, eg ("Allegheny County")?
        // TODO Is this mandatory if county is empty?
        val county = county?.let {
            if (it.isNotEmpty()) {
                CompletePlaceName("County", it)
            } else {
                null
            }
        }

        return listOfNotNull(city, county).nullIfEmpty()
    }

    private fun validateRegistrationData(registrationData: RegistrationData) {
        val errorMsgTemplate = "%s is null"
        val userMessageTemplate = R.string.registration_data_validation_error
        registrationData.newRegistrantData ?: throw InvalidRegistrationException(
            errorMsgTemplate.format("newRegistrantData"),
            userMessageTemplate,
            R.string.fragment_title_new_registrant
        )

        registrationData.addressData ?: throw InvalidRegistrationException(
            errorMsgTemplate.format("addressData"),
            userMessageTemplate,
            R.string.fragment_title_personal_info
        )

        registrationData.additionalInfoData ?: throw InvalidRegistrationException(
            errorMsgTemplate.format("additionalInfoData"),
            userMessageTemplate,
            R.string.fragment_title_additional_info
        )

        registrationData.assistanceData ?: throw InvalidRegistrationException(
            errorMsgTemplate.format("assistanceData"),
            userMessageTemplate,
            R.string.fragment_title_assistant_info
        )

        registrationData.reviewData ?: throw InvalidRegistrationException(
            errorMsgTemplate.format("reviewData"),
            userMessageTemplate,
            R.string.fragment_title_review
        )
    }

    private fun <T> List<T>.nullIfEmpty(): List<T>? {
        return if (isEmpty()) {
            null
        } else {
            this
        }
    }
}