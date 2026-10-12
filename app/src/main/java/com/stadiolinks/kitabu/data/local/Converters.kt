package com.stadiolinks.kitabu.data.local

import androidx.room.TypeConverter
import com.stadiolinks.kitabu.domain.BookingStatus

/** Maps the BookingStatus enum to a SQLite TEXT column and back. */
class Converters {

    @TypeConverter
    fun fromStatus(status: BookingStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): BookingStatus =
        runCatching { BookingStatus.valueOf(value) }.getOrDefault(BookingStatus.PENDING)
}
