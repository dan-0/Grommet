package com.rockthevote.grommet.data.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Created by Mechanical Man on 4/12/20.
 */
@Entity
data class Session(
        @PrimaryKey val id: Int,


        @ColumnInfo(name = "source_tracking_id") val sourceTrackingID: String,

        @ColumnInfo(name = "partner_tracking_id") val partnerTrackingID: String,

//        @ColumnInfo(name="geo_location") val geoLocation: ? ,

        @ColumnInfo(name = "open_tracking_id") val openTrackingID: String,

        @ColumnInfo(name = "canvasser_name") val canvasserName: String,

        @ColumnInfo(name = "device_id") val deviceID: String,

        @ColumnInfo(name = "abandoned_count") val abandonedCount: Int,

        /**
         *         TODO do we even need this if we can just count the number in the registration table?
         *         probably a good idea to keep it since registrations can be uploaded and it would mess the count
         *         if the clock in/out gets sent after a registration gets sent
         */
        @ColumnInfo(name = "registration_count") val registrationCount: Int,

        /**
         * Number of registrants that opted into receiving SMS during a session
         */
        @ColumnInfo(name = "sms_count") val smsCount: Int,

        /**
         * Number of registrants that provided DL info during a session
         */
        @ColumnInfo(name = "drivers_license_count") val driversLicenseCount: Int,

        @ColumnInfo(name = "clock_in_time") val clockInTime: Date,

        @ColumnInfo(name = "clock_out_time") val clockOutTime: Date


)