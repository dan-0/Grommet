package com.rockthevote.grommet.ui.registration.personal

import com.rockthevote.grommet.data.db.model.Party
import com.rockthevote.grommet.data.db.model.PhoneType
import com.rockthevote.grommet.data.db.model.PreferredLanguage
import com.rockthevote.grommet.data.db.model.Race
import com.rockthevote.grommet.databinding.FragmentAdditionalInfoBinding
import com.rockthevote.grommet.ui.registration.name.toEditable

fun FragmentAdditionalInfoBinding.toAdditionalInfoData(): AdditionalInfoData {
    val party = spinnerParty.spinnerText.toString().let {
        Party.fromString(it)
    }

    val emailAddress = emailEditText.text.toString()
    val phoneNumber = phone.text.toString()
    val phoneType = spinnerPhoneType.spinnerText.toString().let {
        PhoneType.fromString(it)
    }

    val hasChangedPoliticalParty = politicalPartyChangeTextbox.isChecked
    val knowsPennDotNumber = !doesNotHavePennDotCheckbox.isChecked
    val knowsSsnLastFour = !ssnLastFourCheckbox.isChecked
    val hasOptedIntoNewsUpdates = emailOptIn.isChecked
    val hasOptedIntoNewsCallAndText = checkboxCanReceiveText.isChecked
    val hasOptedForVolunteerText = checkboxPartnerVolunteerOptIn.isChecked
    val preferredLanguage = spinnerPreferredLanguage.spinnerText?.let {
        PreferredLanguage.fromString(it)
    }
    val race = spinnerRace.spinnerText?.let { Race.fromString(it) }

    val otherPoliticalParty = if (party == Party.OTHER_PARTY) otherPartyEditText.text?.toString() else null
    val pennDotNumber = if (knowsPennDotNumber) pennDotEditText.text?.toString() else null
    val ssnLastFour = if (knowsSsnLastFour )ssnLastFourEditText.text?.toString() else null

    return AdditionalInfoData(
        party = party,
        emailAddress = emailAddress,
        phoneNumber = phoneNumber,
        phoneType = phoneType,
        hasChangedPoliticalParty = hasChangedPoliticalParty,
        knowsPennDotNumber = knowsPennDotNumber,
        knowsSsnLastFour = knowsSsnLastFour,
        hasOptedIntoNewsUpdates = hasOptedIntoNewsUpdates,
        hasOptedIntoNewsCallAndText = hasOptedIntoNewsCallAndText,
        hasOptedForVolunteerText = hasOptedForVolunteerText,
        otherPoliticalParty = otherPoliticalParty,
        pennDotNumber = pennDotNumber,
        ssnLastFour = ssnLastFour,
        preferredLanguage = preferredLanguage,
        race = race
    )
}

fun AdditionalInfoData.toFragmentAdditionalInfoBinding(binding: FragmentAdditionalInfoBinding) {
    with (binding) {
        spinnerParty.setEditText(party.toString())
        emailEditText.text = emailAddress.toEditable()
        phone.text = phoneNumber.toEditable()
        spinnerPhoneType.setEditText(phoneType.toString())
        politicalPartyChangeTextbox.isChecked = hasChangedPoliticalParty
        doesNotHavePennDotCheckbox.isChecked = !knowsPennDotNumber
        ssnLastFourCheckbox.isChecked = !knowsSsnLastFour
        emailOptIn.isChecked = hasOptedIntoNewsUpdates
        checkboxCanReceiveText.isChecked = hasOptedIntoNewsCallAndText
        checkboxPartnerVolunteerOptIn.isChecked = hasOptedForVolunteerText

        preferredLanguage?.let { spinnerPreferredLanguage.setEditText(it.toString()) }
        race?.let { spinnerRace.setEditText(it.toString()) }
    }
}