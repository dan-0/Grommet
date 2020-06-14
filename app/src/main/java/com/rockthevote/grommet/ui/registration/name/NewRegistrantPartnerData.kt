package com.rockthevote.grommet.ui.registration.name

import com.rockthevote.grommet.data.api.model.RegistrationNotificationText
import java.util.*

/**
 * Created by Mechanical Man on 6/14/20.
 */
data class NewRegistrantPartnerData(
        val registrationDeadline: Date,
        val registrationNotificationText: RegistrationNotificationText
)
