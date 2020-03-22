package com.rockthevote.grommet.ui.registration

import com.rockthevote.grommet.ui.registration.address.AddressRegistrationData
import com.rockthevote.grommet.ui.registration.assistance.AssistanceRegistrationData
import com.rockthevote.grommet.ui.registration.name.NameRegistrationData
import com.rockthevote.grommet.ui.registration.personal.AdditionalInfoRegistrationData
import com.rockthevote.grommet.ui.registration.review.ReviewRegistrationData

data class RegistrationData(
    val nameRegistrationData: NameRegistrationData?,
    val addressRegistrationData: AddressRegistrationData?,
    val additionalInfoRegistrationData: AdditionalInfoRegistrationData?,
    val assistanceRegistrationData: AssistanceRegistrationData?,
    val reviewRegistrationData: ReviewRegistrationData?
)