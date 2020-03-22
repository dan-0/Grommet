package com.rockthevote.grommet.ui.registration.address

data class AddressRegistrationData(
    // Mandatory
    val registrationAddress: Address,
    val isMailingAddressDifferent: Boolean,
    val hasPreviousAddress: Boolean,
    // Optional
    val mailingAddress: Address?,
    val previousAddress: Address?
)

