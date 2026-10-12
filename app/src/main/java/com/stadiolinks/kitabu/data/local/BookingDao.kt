package com.stadiolinks.kitabu.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.stadiolinks.kitabu.domain.BookingStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {

    // ---- Create ----
    @Insert
    suspend fun insertBooking(booking: BookingEntity): Long

    // ---- Read ----
    /** Pending and active bookings joined with their book details for the dashboard. */
    @Query(
        "SELECT b.bookingId AS bookingId, b.bookOwnerId AS bookOwnerId, b.userName AS userName, " +
            "b.bookingDate AS bookingDate, b.returnDeadline AS returnDeadline, b.status AS status, " +
            "k.title AS title, k.author AS author, k.category AS category " +
            "FROM bookings b INNER JOIN books k ON b.bookOwnerId = k.bookId " +
            "WHERE b.status IN ('PENDING', 'ACTIVE') " +
            "ORDER BY b.returnDeadline ASC"
    )
    fun getActiveBookingsWithBooks(): Flow<List<BookingWithBook>>

    @Query("SELECT * FROM bookings WHERE bookingId = :bookingId")
    suspend fun getBookingById(bookingId: Long): BookingEntity?

    // ---- Update ----
    @Query("UPDATE bookings SET returnDeadline = :newDeadline WHERE bookingId = :bookingId")
    suspend fun updateReturnDeadline(bookingId: Long, newDeadline: Long)

    @Query("UPDATE bookings SET status = :status WHERE bookingId = :bookingId")
    suspend fun updateStatus(bookingId: Long, status: BookingStatus)

    // ---- Delete ----
    /** Cancels a booking before pickup. Returns the number of rows removed (0 or 1). */
    @Query("DELETE FROM bookings WHERE bookingId = :bookingId AND status = 'PENDING'")
    suspend fun deletePendingBooking(bookingId: Long): Int
}
