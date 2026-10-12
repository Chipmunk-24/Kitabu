package com.stadiolinks.kitabu.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.stadiolinks.kitabu.data.repository.LibraryRepository
import com.stadiolinks.kitabu.ui.bookings.BookingsViewModel
import com.stadiolinks.kitabu.ui.catalog.CatalogViewModel

class KitabuViewModelFactory(private val repository: LibraryRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(CatalogViewModel::class.java) -> CatalogViewModel(repository) as T
        modelClass.isAssignableFrom(BookingsViewModel::class.java) -> BookingsViewModel(repository) as T
        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
