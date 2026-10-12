package com.stadiolinks.kitabu.ui.bookings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stadiolinks.kitabu.data.local.BookingWithBook
import com.stadiolinks.kitabu.domain.BookingStatus
import com.stadiolinks.kitabu.domain.daysRemaining
import com.stadiolinks.kitabu.domain.formatDate
import com.stadiolinks.kitabu.ui.components.BookCover
import com.stadiolinks.kitabu.ui.components.EmptyState
import com.stadiolinks.kitabu.ui.components.StatusBadge
import com.stadiolinks.kitabu.ui.theme.ActiveBlue
import com.stadiolinks.kitabu.ui.theme.AvailableGreen
import com.stadiolinks.kitabu.ui.theme.PendingAmber

@Composable
fun BookingsScreen(
    bookings: List<BookingWithBook>,
    onRenew: (Long) -> Unit,
    onReturn: (Long) -> Unit,
    onCancel: (Long) -> Unit,
    onConfirmPickup: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val pending = bookings.count { it.status == BookingStatus.PENDING }
    val active = bookings.count { it.status == BookingStatus.ACTIVE }

    Column(modifier = modifier) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp)) {
            Text(
                text = "My Reservations",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (bookings.isEmpty()) "Nothing reserved yet" else "$active active · $pending awaiting pickup",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (bookings.isEmpty()) {
            EmptyState(
                icon = Icons.Default.DateRange,
                title = "No active reservations",
                message = "Reserve a book from the catalog and it will show up here."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
    val days = daysRemaining(booking.returnDeadline)
    val urgencyColor = when {
        days < 0 -> MaterialTheme.colorScheme.error
        days <= 2 -> PendingAmber
        else -> AvailableGreen
    }
    val total = (booking.returnDeadline - booking.bookingDate).coerceAtLeast(1L)
    val elapsed = (System.currentTimeMillis() - booking.bookingDate).coerceIn(0L, total)
    val progress = elapsed.toFloat() / total.toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Book summary
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                BookCover(
                    title = booking.title,
                    category = booking.category,
                    modifier = Modifier.size(width = 56.dp, height = 76.dp).clip(RoundedCornerShape(12.dp)),
                    letterSize = 26.sp
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = booking.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = booking.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Reserved by ${booking.userName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    StatusBadge(
                        text = if (isPending) "Awaiting pickup" else "Active",
                        color = if (isPending) PendingAmber else ActiveBlue
                    )
                }
            }

            // Dates and time left
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    DateColumn("Reserved", formatDate(booking.bookingDate), Modifier.weight(1f))
                    DateColumn("Return by", formatDate(booking.returnDeadline), Modifier.weight(1f))
                }
                Text(
                    text = daysLabel(days),
                    style = MaterialTheme.typography.titleSmall,
                    color = urgencyColor
                )
                LinearProgressIndicator(
                    progress = progress,
                    color = urgencyColor,
                    trackColor = urgencyColor.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                )
            }

            // Lifecycle actions depend on the status
            if (isPending) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onConfirmPickup,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1.4f)
                    ) { Text("Confirm pickup", maxLines = 1) }
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancel", maxLines = 1) }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onRenew,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) { Text("Renew", maxLines = 1) }
                    FilledTonalButton(
                        onClick = onReturn,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) { Text("Return", maxLines = 1) }
                }
            }
        }
    }
}

@Composable
private fun DateColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.titleSmall)
    }
}

private fun daysLabel(days: Long): String = when {
    days > 1 -> "$days days remaining"
    days == 1L -> "1 day remaining"
    days == 0L -> "Due today"
    days == -1L -> "1 day overdue"
    else -> "${-days} days overdue"
}
