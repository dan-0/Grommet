package com.rockthevote.grommet.data.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for registrations in the database. Autogenerates a [uid], taking in a
 * [registrationDate] in millis, [registrantName] and [registrantEmail] for identification,
 * and a full json string as [registrationData]
 */
@Entity(tableName = "registration")
data class Registration(
    @PrimaryKey(autoGenerate = true)
    val uid: Long = 0,
    @ColumnInfo(name = "registration_date")
    val registrationDate: Long,
    @ColumnInfo(name = "registrant_name")
    val registrantName: String,
    @ColumnInfo(name = "registrant_email")
    val registrantEmail: String,
    @ColumnInfo(name = "registration_data")
    val registrationData: String
)