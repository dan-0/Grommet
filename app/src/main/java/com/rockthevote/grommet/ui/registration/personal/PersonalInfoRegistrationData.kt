package com.rockthevote.grommet.ui.registration.personal

import com.rockthevote.grommet.data.db.model.Party
import com.rockthevote.grommet.data.db.model.PhoneType
import com.rockthevote.grommet.data.db.model.PreferredLanguage
import com.rockthevote.grommet.data.db.model.Race


data class PersonalInfoRegistrationData(
    // Mandatory
    val party: Party,
    val emailAddress: String,
    val phoneNumber: String,
    val phoneType: PhoneType,
    val hasChangedPoliticalParty: Boolean,
    val knowsPennDotNumber: Boolean,
    val knowsSsnLastFour: Boolean,
    val hasOptedIntoNewsUpdates: Boolean,
    val hasOptedIntoNewsCallAndText: Boolean,
    val hasOptedForVolunteerText: Boolean,
    // Optional
    val preferredLanguage: PreferredLanguage?,
    val race: Race?
)