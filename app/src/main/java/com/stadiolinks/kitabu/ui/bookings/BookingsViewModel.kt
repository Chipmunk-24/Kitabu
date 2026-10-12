package com.stadiolinks.kitabu.ui.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stadiolinks.kitabu.data.local.BookingWithBook
import com.stadiolinks.kitabu.data.repository.LibraryRepository
import com.stadiolinks.kitabu.data.repository.toUserMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookingsViewModel(private val repository: LibraryRepository) : ViewModel() {

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    /** Pending + active bookings joined with book details, straight from Room as a Flow. */
    val bookings: StateFlow<List<BookingWithBook>> =
        repository.observeActiveBookings()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun confirmPickup(bookingId: Long) =
        perform("Pickup confirmed. Enjoy your book!") { repository.confirmPickup(bookingId) }

    fun renew(bookingId: Long) =
        perform("Rental extended by 7 days") { repository.renewBooking(bookingId) }

    fun returnBook(bookingId: Long) =
        perform("Book returned. Thank you!") { repository.returnBook(bookingId) }

    fun cancel(bookingId: Long) =
        perform("Reservation cancelled") { repository.cancelBooking(bookingId) }

    private fun perform(successMessage: String, action: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            action().fold(
                onSuccess = { _messages.emit(successMessage) },
                onFailure = { _messages.emit(it.toUserMessage()) }
            )
        }
    }
}
