package com.stadiolinks.kitabu.data.local

import com.stadiolinks.kitabu.domain.BookingStatus

/** Result of the bookings JOIN books query used by the reservations dashboard. */
data class BookingWithBook(
    val bookingId: Long,
    val bookOwnerId: Long,
    val userName: String,
    val bookingDate: Long,
    val returnDeadline: Long,
    val status: BookingStatus,
    val title: String,
    val author: String,
    val category: String
)
