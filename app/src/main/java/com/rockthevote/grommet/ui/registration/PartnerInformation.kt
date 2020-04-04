package com.rockthevote.grommet.ui.registration

// TODO Find exactly what we need to provide so a request can be made from the VM
/**
 * Partner information for generating a RockyRequest
 */
data class PartnerInformation(
    val partnerId: Int,
    val canvasserName: String
)