package com.rockthevote.grommet.ui.registration

import com.rockthevote.grommet.ui.registration.address.AddressRegistrationData
import com.rockthevote.grommet.ui.registration.assistance.AssistanceRegistrationData
import com.rockthevote.grommet.ui.registration.name.NameRegistrationData
import com.rockthevote.grommet.ui.registration.personal.PersonalInfoRegistrationData
import com.rockthevote.grommet.ui.registration.review.ReviewRegistrationData

data class RegistrationData(
    val nameRegistrationData: NameRegistrationData?,
    val addressRegistrationData: AddressRegistrationData?,
    val personalInfoRegistrationData: PersonalInfoRegistrationData?,
    val assistanceRegistrationData: AssistanceRegistrationData?,
    val reviewRegistrationData: ReviewRegistrationData?
)