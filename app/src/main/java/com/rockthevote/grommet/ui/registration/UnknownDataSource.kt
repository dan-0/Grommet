package com.rockthevote.grommet.ui.registration

import com.rockthevote.grommet.data.db.model.GeoLocation

/**
 * TODO: I don't know where this data is derived from?
 *
 * DO NOT MERGE THIS, it is a placeholder while I figure out where data comes from
 * for the API
 */
data class UnknownDataSource(
    val partnerOptInEmail: Boolean,
    val partnerOptInSms: Boolean,
    val partnerOptInVolunteer: Boolean,
    val sourceTrackingId: String,
    val partnerTrackingId: String,
    val geoLocation: GeoLocation,
    val openTrackingId: String
)