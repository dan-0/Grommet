package com.rockthevote.grommet.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Mechanical Man on 4/1/20.
 */
@Entity
data class Registration(
        @PrimaryKey val id: Int,
        @ColumnInfo(name = "registration") val firstName: String?
)