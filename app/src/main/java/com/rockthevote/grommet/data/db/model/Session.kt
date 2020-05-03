package com.rockthevote.grommet.data.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.rockthevote.grommet.data.api.model.ApiGeoLocation
import java.util.*

/**
 * Created by Mechanical Man on 4/12/20.
 */
@Entity(tableName = "session")
data class Session(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "session_id")
        val sessionId: Long = 0,

        @ColumnInfo(name = "partner_info_id")
        val partnerInfoId: Long,

        @ColumnInfo(name = "source_tracking_id")
        val sourceTrackingId: String,

        @ColumnInfo(name = "partner_tracking_id")
        val partnerTrackingId: String,

        @ColumnInfo(name = "geo_location") val geoLocation: ApiGeoLocation,

        @ColumnInfo(name = "open_tracking_id")
        val openTrackingId: String,

        @ColumnInfo(name = "canvasser_name")
        val canvasserName: String,

        @ColumnInfo(name = "device_id")
        val deviceId: String,

        @ColumnInfo(name = "abandoned_count")
        val abandonedCount: Int,

        /**
         *         TODO do we even need this if we can just count the number in the registration table?
         *         probably a good idea to keep it since registrations can be uploaded and it would mess the count
         *         if the clock in/out gets sent after a registration gets sent
         */
        @ColumnInfo(name = "registration_count")
        val registrationCount: Int,

        /**
         * Number of registrants that opted into receiving SMS during a session
         */
        @ColumnInfo(name = "sms_count")
        val smsCount: Int,

        /**
         * Number of registrants that provided DL info during a session
         */
        @ColumnInfo(name = "drivers_license_count")
        val driversLicenseCount: Int,

        @ColumnInfo(name = "clock_in_time")
        val clockInTime: Date,

        @ColumnInfo(name = "clock_out_time")
        val clockOutTime: Date,

        @ColumnInfo(name = "session_status")
        val sessionStatus: SessionStatus
)