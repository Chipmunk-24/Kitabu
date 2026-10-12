package com.stadiolinks.kitabu.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stadiolinks.kitabu.data.local.BookEntity
import com.stadiolinks.kitabu.data.repository.LibraryRepository
import com.stadiolinks.kitabu.data.repository.toUserMessage
import com.stadiolinks.kitabu.domain.AvailabilityFilter
import com.stadiolinks.kitabu.domain.InputValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CatalogUiState(
    val query: String = "",
    val filter: AvailabilityFilter = AvailabilityFilter.ALL,
    val books: List<BookEntity> = emptyList()
)

class CatalogViewModel(private val repository: LibraryRepository) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(AvailabilityFilter.ALL)

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    /** One-off messages (snackbars) for the UI. */
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    /** Room Flow -> StateFlow. Re-queries whenever the search text or filter changes. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val books = combine(query, filter) { q, f -> q to f }
        .flatMapLatest { (q, f) -> repository.observeBooks(q, f) }

    val uiState: StateFlow<CatalogUiState> =
        combine(query, filter, books) { q, f, b -> CatalogUiState(q, f, b) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CatalogUiState())

    fun onQueryChange(value: String) {
        query.value = value.take(InputValidator.MAX_QUERY)
    }

    fun onFilterChange(value: AvailabilityFilter) {
        filter.value = value
    }

    fun reserveBook(bookId: Long, userName: String, durationDays: Int) {
        viewModelScope.launch {
            repository.reserveBook(bookId, userName, durationDays).fold(
                onSuccess = { _messages.emit("Reserved! Find it under My Reservations.") },
                onFailure = { _messages.emit(it.toUserMessage()) }
            )
        }
    }

    fun addBook(title: String, author: String, category: String) {
        viewModelScope.launch {
            repository.addBook(title, author, category).fold(
                onSuccess = { _messages.emit("Book added to the catalog") },
                onFailure = { _messages.emit(it.toUserMessage()) }
            )
        }
    }
}
