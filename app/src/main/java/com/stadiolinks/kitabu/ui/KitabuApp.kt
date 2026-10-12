package com.stadiolinks.kitabu.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.stadiolinks.kitabu.ui.bookings.BookingsViewModel
import com.stadiolinks.kitabu.ui.catalog.CatalogViewModel
import com.stadiolinks.kitabu.ui.navigation.KitabuBottomBar
import com.stadiolinks.kitabu.ui.navigation.KitabuNavHost
import kotlinx.coroutines.flow.merge

/** Single-activity root: one Scaffold, one NavHost, ViewModels shared across both destinations. */
@Composable
fun KitabuApp(viewModelFactory: ViewModelProvider.Factory) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val catalogViewModel: CatalogViewModel = viewModel(factory = viewModelFactory)
    val bookingsViewModel: BookingsViewModel = viewModel(factory = viewModelFactory)

    LaunchedEffect(catalogViewModel, bookingsViewModel) {
        merge(catalogViewModel.messages, bookingsViewModel.messages).collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { KitabuBottomBar(navController) }
    ) { innerPadding ->
        KitabuNavHost(
            navController = navController,
            catalogViewModel = catalogViewModel,
            bookingsViewModel = bookingsViewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
