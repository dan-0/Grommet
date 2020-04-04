package com.rockthevote.grommet.ui.registration

/**
 * Provided registration data is invalid.
 */
class InvalidRegistrationException(
    msg: String,
    val userMessage: String
) : Exception(msg)