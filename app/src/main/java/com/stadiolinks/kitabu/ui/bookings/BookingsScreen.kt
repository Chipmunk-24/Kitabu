package com.stadiolinks.kitabu.ui.bookings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stadiolinks.kitabu.data.local.BookingWithBook
import com.stadiolinks.kitabu.domain.BookingStatus
import com.stadiolinks.kitabu.domain.daysRemaining
import com.stadiolinks.kitabu.domain.formatDate
import com.stadiolinks.kitabu.ui.components.EmptyState
import com.stadiolinks.kitabu.ui.components.NoInsets
import com.stadiolinks.kitabu.ui.components.StatusBadge
import com.stadiolinks.kitabu.ui.theme.ActiveBlue
import com.stadiolinks.kitabu.ui.theme.PendingAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    bookings: List<BookingWithBook>,
    onRenew: (Long) -> Unit,
    onReturn: (Long) -> Unit,
    onCancel: (Long) -> Unit,
    onConfirmPickup: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        TopAppBar(
            title = { Text("My Reservations", fontWeight = FontWeight.Bold) },
            windowInsets = NoInsets
        )

        if (bookings.isEmpty()) {
            EmptyState("No active reservations.\nReserve a book from the catalog to see it here.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bookings, key = { it.bookingId }) { booking ->
                    BookingCard(
                        booking = booking,
                        onRenew = { onRenew(booking.bookingId) },
                        onReturn = { onReturn(booking.bookingId) },
                        onCancel = { onCancel(booking.bookingId) },
                        onConfirmPickup = { onConfirmPickup(booking.bookingId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookingCard(
    booking: BookingWithBook,
    onRenew: () -> Unit,
    onReturn: () -> Unit,
    onCancel: () -> Unit,
    onConfirmPickup: () -> Unit
) {
    val isPending = booking.status == BookingStatus.PENDING
    val isActive = booking.status == BookingStatus.ACTIVE
    val days = daysRemaining(booking.returnDeadline)
    val buttonPadding = PaddingValues(horizontal = 8.dp)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(booking.title, style = MaterialTheme.typography.titleMedium)
                    Text(booking.author, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        booking.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                StatusBadge(
                    text = if (isPending) "Pending" else "Active",
                    color = if (isPending) PendingAmber else ActiveBlue
                )
            }

            Text("Reserved by ${booking.userName}", style = MaterialTheme.typography.bodySmall)
            Text("Reserved on: ${formatDate(booking.bookingDate)}", style = MaterialTheme.typography.bodyMedium)
            Text("Return by: ${formatDate(booking.returnDeadline)}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = daysLabel(days),
                style = MaterialTheme.typography.titleSmall,
                color = if (days < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )

            if (isPending) {
                FilledTonalButton(onClick = onConfirmPickup, modifier = Modifier.fillMaxWidth()) {
                    Text("Confirm pickup")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRenew,
                    enabled = isActive,
                    contentPadding = buttonPadding,
                    modifier = Modifier.weight(1f)
                ) { Text("Renew", maxLines = 1) }

                OutlinedButton(
                    onClick = onReturn,
                    enabled = isActive,
                    contentPadding = buttonPadding,
                    modifier = Modifier.weight(1f)
                ) { Text("Return", maxLines = 1) }

                OutlinedButton(
                    onClick = onCancel,
                    enabled = isPending,
                    contentPadding = buttonPadding,
                    modifier = Modifier.weight(1f)
                ) { Text("Cancel", maxLines = 1) }
            }
        }
    }
}

private fun daysLabel(days: Long): String = when {
    days > 1 -> "$days days remaining"
    days == 1L -> "1 day remaining"
    days == 0L -> "Due today"
    days == -1L -> "1 day overdue"
    else -> "${-days} days overdue"
}
