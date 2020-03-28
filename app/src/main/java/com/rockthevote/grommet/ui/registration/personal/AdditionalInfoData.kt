package com.rockthevote.grommet.ui.registration.personal

import com.rockthevote.grommet.data.db.model.Party
import com.rockthevote.grommet.data.db.model.PhoneType
import com.rockthevote.grommet.data.db.model.PreferredLanguage
import com.rockthevote.grommet.data.db.model.Race


data class AdditionalInfoData(
    // Mandatory
    val party: Party,
    val emailAddress: String,
    val phoneNumber: String,
    val phoneType: PhoneType,
    val hasChangedPoliticalParty: Boolean,
    val knowsPennDotNumber: Boolean,
    val knowsSsnLastFour: Boolean,
    // email_opt_in
    val hasOptedIntoNewsUpdates: Boolean,
    // checkbox_can_receive_text
    val hasOptedIntoNewsCallAndText: Boolean,
    // checkbox_partner_volunteer_opt_in
    val hasOptedForVolunteerText: Boolean,
    // Optional
    val preferredLanguage: PreferredLanguage?,
    val race: Race?
)