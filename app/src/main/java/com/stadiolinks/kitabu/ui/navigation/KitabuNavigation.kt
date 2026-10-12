package com.stadiolinks.kitabu.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.stadiolinks.kitabu.ui.bookings.BookingsScreen
import com.stadiolinks.kitabu.ui.bookings.BookingsViewModel
import com.stadiolinks.kitabu.ui.catalog.CatalogScreen
import com.stadiolinks.kitabu.ui.catalog.CatalogViewModel

enum class Screen(val route: String, val label: String, val icon: ImageVector) {
    Catalog("catalog", "Catalog", Icons.Default.Home),
    Bookings("bookings", "My Reservations", Icons.Default.DateRange)
}

@Composable
fun KitabuNavHost(
    navController: NavHostController,
    catalogViewModel: CatalogViewModel,
    bookingsViewModel: BookingsViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Catalog.route,
        modifier = modifier
    ) {
        composable(Screen.Catalog.route) {
            // Lifecycle-aware collection of the StateFlow built from Room's Flow.
            val uiState by catalogViewModel.uiState.collectAsStateWithLifecycle()
            CatalogScreen(
                uiState = uiState,
                onQueryChange = catalogViewModel::onQueryChange,
                onFilterChange = catalogViewModel::onFilterChange,
                onReserve = catalogViewModel::reserveBook,
                onAddBook = catalogViewModel::addBook
            )
        }
        composable(Screen.Bookings.route) {
            val bookings by bookingsViewModel.bookings.collectAsStateWithLifecycle()
            BookingsScreen(
                bookings = bookings,
                onRenew = bookingsViewModel::renew,
                onReturn = bookingsViewModel::returnBook,
                onCancel = bookingsViewModel::cancel,
                onConfirmPickup = bookingsViewModel::confirmPickup
            )
        }
    }
}

@Composable
fun KitabuBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        Screen.entries.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) }
            )
        }
    }
}
