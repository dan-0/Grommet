package com.rockthevote.grommet.ui.registration

import androidx.annotation.StringRes

sealed class RegistrationState {
    object Init: RegistrationState()
    object InProgress : RegistrationState()
    object Complete : RegistrationState()
    class RegistrationError(
        @StringRes val errorMsg: Int,
        @StringRes vararg formatVar: Int?
    ): RegistrationState()
}