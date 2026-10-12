# Kitabu - Library Book Rental & Reservation System (MADB372 SF1)

Native Android app for browsing a library catalog, reserving books and managing rentals
(renew, return, cancel). Built with Kotlin, Jetpack Compose (Material 3), Room, Coroutines and Flow.

## Setup
1. Install Android Studio (Ladybug or newer) with JDK 17+.
2. Clone the repository and open the project folder in Android Studio.
3. Let Gradle sync finish (Kotlin 1.9.24, KSP 1.9.24-1.0.20, Room 2.6.1).
4. Run on an emulator or device with Android 8.0 (API 26) or higher.

The Room database is created and pre-populated with 12 sample books (plus two demo bookings) on first launch.
No internet, Firebase or login is needed.

## Architecture (MVVM)
```
com.stadiolinks.kitabu
 |- ui/         Compose screens, ViewModels, navigation, theme
 |   |- catalog/  CatalogScreen + CatalogViewModel
 |   |- bookings/ BookingsScreen + BookingsViewModel
 |- domain/     BookingStatus, RentalPolicy, InputValidator, date helpers
 |- data/
 |   |- local/      BookEntity, BookingEntity, DAOs, Converters, KitabuDatabase
 |   |- repository/ LibraryRepository (business rules + transactions)
 |- di/         AppContainer + ViewModel factory (manual DI)
```
Flow: Room DAO `Flow` -> Repository -> ViewModel `StateFlow` (`stateIn`) -> `collectAsStateWithLifecycle()` -> Compose.
All writes run in `viewModelScope.launch` and Room moves them off the main thread.

## CRUD / lifecycle
| Action | Where | Database effect |
|---|---|---|
| Create | Reserve (bottom sheet), Add book | Insert `BookingEntity` (PENDING) and mark book unavailable; insert `BookEntity` |
| Read | Catalog, My Reservations | Search with SQL `LIKE`, availability filter, bookings JOIN books |
| Update | Confirm pickup, Renew, Return | PENDING to ACTIVE; extend `returnDeadline`; RETURNED and book available again |
| Delete | Cancel | Delete the PENDING booking row and free the book |

## Screenshots
_Add CRUD screenshots / GIFs here._
