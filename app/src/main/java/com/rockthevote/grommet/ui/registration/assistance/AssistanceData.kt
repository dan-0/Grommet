package com.rockthevote.grommet.ui.registration.assistance

import com.rockthevote.grommet.ui.registration.address.Address
import com.rockthevote.grommet.ui.registration.name.PersonName

data class AssistanceData(
    // Mandatory
    val hasSomeoneAssisted: Boolean,
    // Optional
    val helperName: PersonName?,
    val helperAddress: Address?,
    val helperPhone: String?,
    val hasConfirmedTerms: Boolean
)