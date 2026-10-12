package com.stadiolinks.kitabu.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.stadiolinks.kitabu.domain.BookingStatus

@Entity(
    tableName = "bookings",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["bookId"],
            childColumns = ["bookOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookOwnerId")]
)
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val bookingId: Long = 0,
    val bookOwnerId: Long,
    val userName: String,
    val bookingDate: Long,
    val returnDeadline: Long,
    val status: BookingStatus = BookingStatus.PENDING
)
