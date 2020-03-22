package com.rockthevote.grommet.ui.registration.name

import java.util.*

data class NameRegistrationData(
    // Required Fields
    val name: PersonName,
    val birthday: Date,
    val isUsCitizen: Boolean,
    val is18OrOlderByNextElection: Boolean,
    // Optional
    val hasChangedName: Boolean,
    val previousName: PersonName?
)