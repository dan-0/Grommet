package com.rockthevote.grommet.data.db

import androidx.room.TypeConverter
import java.util.*

/**
 * Created by Mechanical Man on 4/12/20.
 */
class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

}