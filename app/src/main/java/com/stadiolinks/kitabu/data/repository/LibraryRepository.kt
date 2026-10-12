package com.stadiolinks.kitabu.data.repository

import androidx.room.withTransaction
import com.stadiolinks.kitabu.data.local.BookEntity
import com.stadiolinks.kitabu.data.local.BookingEntity
import com.stadiolinks.kitabu.data.local.BookingWithBook
import com.stadiolinks.kitabu.data.local.KitabuDatabase
import com.stadiolinks.kitabu.domain.AvailabilityFilter
import com.stadiolinks.kitabu.domain.BookingStatus
import com.stadiolinks.kitabu.domain.InputValidator
import com.stadiolinks.kitabu.domain.RentalPolicy
import com.stadiolinks.kitabu.domain.ValidationResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Single source of truth for the UI. All business rules live here; every write that touches
 * more than one table runs inside one Room transaction.
 */
class LibraryRepository(private val db: KitabuDatabase) {

    private val bookDao = db.bookDao()
    private val bookingDao = db.bookingDao()

    // ---------- Reads (Flow straight from Room) ----------

    fun observeBooks(query: String, filter: AvailabilityFilter): Flow<List<BookEntity>> {
        val cleaned = InputValidator.sanitize(query, InputValidator.MAX_QUERY)
        if (cleaned.isEmpty()) {
            return when (filter) {
                AvailabilityFilter.ALL -> bookDao.getAllBooks()
                AvailabilityFilter.AVAILABLE -> bookDao.getBooksByAvailability(true)
                AvailabilityFilter.BORROWED -> bookDao.getBooksByAvailability(false)
            }
        }
        return bookDao.searchBooks(InputValidator.escapeLike(cleaned)).map { books ->
            when (filter) {
                AvailabilityFilter.ALL -> books
                AvailabilityFilter.AVAILABLE -> books.filter { it.isAvailable }
                AvailabilityFilter.BORROWED -> books.filterNot { it.isAvailable }
            }
        }
    }

    fun observeActiveBookings(): Flow<List<BookingWithBook>> = bookingDao.getActiveBookingsWithBooks()

    // ---------- Create ----------

    suspend fun addBook(title: String, author: String, category: String): Result<Long> = attempt {
        val book = BookEntity(
            title = InputValidator.validateRequired(title, "Title", InputValidator.MAX_TITLE).getOrThrow(),
            author = InputValidator.validateRequired(author, "Author", InputValidator.MAX_AUTHOR).getOrThrow(),
            category = InputValidator.validateRequired(category, "Category", InputValidator.MAX_CATEGORY).getOrThrow()
        )
        bookDao.insertBook(book)
    }

    suspend fun reserveBook(
        bookId: Long,
        userName: String,
        durationDays: Int,
        now: Long = System.currentTimeMillis()
    ): Result<Long> = attempt {
        val name = InputValidator.validateUserName(userName).getOrThrow()
        require(durationDays in RentalPolicy.durationOptionsDays) { "Choose a valid rental duration" }
        val deadline = now + durationDays * RentalPolicy.DAY_MILLIS
        require(deadline > now) { "The return date must be after the booking date" }

        db.withTransaction {
            val book = bookDao.getBookById(bookId) ?: throw IllegalStateException("Book not found")
            check(book.isAvailable) { "\"${book.title}\" has just been reserved by someone else" }
            val bookingId = bookingDao.insertBooking(
                BookingEntity(
                    bookOwnerId = bookId,
                    userName = name,
                    bookingDate = now,
                    returnDeadline = deadline,
                    status = BookingStatus.PENDING
                )
            )
            bookDao.setAvailability(bookId, false)
            bookingId
        }
    }

    // ---------- Update ----------

    /** PENDING -> ACTIVE (the student collected the book). */
    suspend fun confirmPickup(bookingId: Long): Result<Unit> = attempt {
        db.withTransaction {
            val booking = bookingDao.getBookingById(bookingId) ?: throw IllegalStateException("Booking not found")
            check(booking.status == BookingStatus.PENDING) { "Only pending reservations can be picked up" }
            bookingDao.updateStatus(bookingId, BookingStatus.ACTIVE)
        }
    }

    /** Extends the return deadline. The new deadline can never be earlier than the old one. */
    suspend fun renewBooking(
        bookingId: Long,
        now: Long = System.currentTimeMillis()
    ): Result<Unit> = attempt {
        db.withTransaction {
            val booking = bookingDao.getBookingById(bookingId) ?: throw IllegalStateException("Booking not found")
            check(booking.status == BookingStatus.ACTIVE) { "Only active rentals can be renewed" }
            val newDeadline = maxOf(booking.returnDeadline, now) + RentalPolicy.RENEWAL_DAYS * RentalPolicy.DAY_MILLIS
            require(newDeadline > booking.returnDeadline) { "Invalid return date" }
            bookingDao.updateReturnDeadline(bookingId, newDeadline)
        }
    }

    /** ACTIVE -> RETURNED and the book becomes available again. */
    suspend fun returnBook(bookingId: Long): Result<Unit> = attempt {
        db.withTransaction {
            val booking = bookingDao.getBookingById(bookingId) ?: throw IllegalStateException("Booking not found")
            check(booking.status == BookingStatus.ACTIVE) { "Only active rentals can be returned" }
            bookingDao.updateStatus(bookingId, BookingStatus.RETURNED)
            bookDao.setAvailability(booking.bookOwnerId, true)
        }
    }

    // ---------- Delete ----------

    /** Deletes a PENDING booking row and frees the book. */
    suspend fun cancelBooking(bookingId: Long): Result<Unit> = attempt {
        db.withTransaction {
            val booking = bookingDao.getBookingById(bookingId) ?: throw IllegalStateException("Booking not found")
            check(booking.status == BookingStatus.PENDING) { "Only pending reservations can be cancelled" }
            check(bookingDao.deletePendingBooking(bookingId) == 1) { "Booking could not be cancelled" }
            bookDao.setAvailability(booking.bookOwnerId, true)
        }
    }

    // ---------- helpers ----------

    private suspend fun <T> attempt(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun ValidationResult.getOrThrow(): String = when (this) {
        is ValidationResult.Valid -> value
        is ValidationResult.Invalid -> throw IllegalArgumentException(message)
    }
}

/** Business-rule errors carry a friendly message; anything else gets a generic one. */
fun Throwable.toUserMessage(): String =
    if (this is IllegalArgumentException || this is IllegalStateException) {
        message ?: "Something went wrong"
    } else {
        "Something went wrong. Please try again."
    }
