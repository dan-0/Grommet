package com.rockthevote.grommet.ui.registration

import com.rockthevote.grommet.ui.registration.address.AddressRegistrationData
import com.rockthevote.grommet.ui.registration.assistance.AssistanceRegistrationData
import com.rockthevote.grommet.ui.registration.name.NameRegistrationData
import com.rockthevote.grommet.ui.registration.personal.AdditionalInfoRegistrationData
import com.rockthevote.grommet.ui.registration.review.ReviewRegistrationData

data class RegistrationData(
    val nameRegistrationData: NameRegistrationData? = null,
    val addressRegistrationData: AddressRegistrationData? = null,
    val additionalInfoRegistrationData: AdditionalInfoRegistrationData? = null,
    val assistanceRegistrationData: AssistanceRegistrationData? = null,
    val reviewRegistrationData: ReviewRegistrationData? = null
)