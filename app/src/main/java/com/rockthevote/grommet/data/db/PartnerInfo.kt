package com.rockthevote.grommet.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Created by Mechanical Man on 4/12/20.
 */
@Entity
data class PartnerInfo(

        @ColumnInfo(name = "app_version") val appVersion: Float,

        @ColumnInfo(name = "is_valid") val isValid: Boolean,

        @ColumnInfo(name = "partner_name") val partnerName: String,

        @ColumnInfo(name = "registration_deadline_date") val registrationDeadlineDate: String,

        )