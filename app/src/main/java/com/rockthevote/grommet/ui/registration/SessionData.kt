package com.rockthevote.grommet.ui.registration

import com.rockthevote.grommet.data.db.model.GeoLocation

/**
 * TODO: Put this into a more appropriate place, this represents session data needed for
 *  generation of a RockyRequest. This data will likely be derived from the current session's
 *  database entry
 */
data class SessionData(
    val partnerId: Long,
    val canvasserName: String,
    val sourceTrackingId: String,
    val partnerTrackingId: String,
    val geoLocation: GeoLocation,
    val openTrackingId: String
)