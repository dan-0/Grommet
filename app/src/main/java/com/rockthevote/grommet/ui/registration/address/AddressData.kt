package com.rockthevote.grommet.ui.registration.address

data class AddressData(
    // Mandatory
    val homeAddress: Address,
    val isMailingAddressDifferent: Boolean,
    val hasPreviousAddress: Boolean,
    // Optional
    val mailingAddress: Address?,
    val previousAddress: Address?
)

