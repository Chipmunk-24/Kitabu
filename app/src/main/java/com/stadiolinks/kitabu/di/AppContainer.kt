package com.stadiolinks.kitabu.di

import android.content.Context
import com.stadiolinks.kitabu.data.local.KitabuDatabase
import com.stadiolinks.kitabu.data.repository.LibraryRepository

/** Manual dependency injection: builds the database, repository and ViewModel factory once. */
class AppContainer(context: Context) {
    private val database: KitabuDatabase by lazy { KitabuDatabase.getInstance(context) }
    val repository: LibraryRepository by lazy { LibraryRepository(database) }
    val viewModelFactory: KitabuViewModelFactory by lazy { KitabuViewModelFactory(repository) }
}
