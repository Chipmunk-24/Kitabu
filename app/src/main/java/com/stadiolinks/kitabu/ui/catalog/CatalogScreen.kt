package com.stadiolinks.kitabu.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stadiolinks.kitabu.data.local.BookEntity
import com.stadiolinks.kitabu.domain.AvailabilityFilter
import com.stadiolinks.kitabu.domain.InputValidator
import com.stadiolinks.kitabu.domain.RentalPolicy
import com.stadiolinks.kitabu.domain.ValidationResult
import com.stadiolinks.kitabu.domain.formatDate
import com.stadiolinks.kitabu.ui.components.BookCover
import com.stadiolinks.kitabu.ui.components.EmptyState
import com.stadiolinks.kitabu.ui.components.StatusBadge
import com.stadiolinks.kitabu.ui.theme.AvailableGreen
import com.stadiolinks.kitabu.ui.theme.BorrowedRed
import kotlinx.coroutines.launch

/** Stateless: everything comes in as parameters and every action goes out as a callback (state hoisting). */
@Composable
fun CatalogScreen(
    uiState: CatalogUiState,
    onQueryChange: (String) -> Unit,
    onFilterChange: (AvailabilityFilter) -> Unit,
    onReserve: (bookId: Long, userName: String, durationDays: Int) -> Unit,
    onAddBook: (title: String, author: String, category: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedBook by remember { mutableStateOf<BookEntity?>(null) }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    val availableCount = uiState.books.count { it.isAvailable }

    Column(modifier = modifier) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Kitabu",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${uiState.books.size} books · $availableCount available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalIconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add book")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Search
        TextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            placeholder = { Text("Search by title or author") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (uiState.query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            )
        )

        // Filter chips
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AvailabilityFilter.entries.forEach { option ->
                FilterChip(
                    selected = uiState.filter == option,
                    onClick = { onFilterChange(option) },
                    label = { Text(option.label) },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        if (uiState.books.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Search,
                title = "No books found",
                message = "Try a different search or filter."
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(uiState.books, key = { it.bookId }) { book ->
                    BookCard(book = book, onClick = { selectedBook = book })
                }
            }
        }
    }

    selectedBook?.let { book ->
        ReserveSheet(
            book = book,
            onDismiss = { selectedBook = null },
            onConfirm = { name, days -> onReserve(book.bookId, name, days) }
        )
    }

    if (showAddDialog) {
        AddBookDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, author, category ->
                onAddBook(title, author, category)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookCard(book: BookEntity, onClick: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Card(
        onClick = onClick,
        enabled = book.isAvailable, // only available books can be reserved
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledContentColor = onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, disabledElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        BookCover(
            title = book.title,
            category = book.category,
            modifier = Modifier.fillMaxWidth().height(104.dp),
            dimmed = !book.isAvailable
        )
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = book.category.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleSmall,
                color = onSurface,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            StatusBadge(
                text = if (book.isAvailable) "Available" else "Borrowed",
                color = if (book.isAvailable) AvailableGreen else BorrowedRed
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReserveSheet(
    book: BookEntity,
    onDismiss: () -> Unit,
    onConfirm: (userName: String, durationDays: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val now = remember { System.currentTimeMillis() }
    var name by rememberSaveable { mutableStateOf("") }
    var days by rememberSaveable { mutableStateOf(RentalPolicy.durationOptionsDays.first()) }

    val validation = InputValidator.validateUserName(name)
    val valid = validation as? ValidationResult.Valid
    val error = (validation as? ValidationResult.Invalid)?.message

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                BookCover(
                    title = book.title,
                    category = book.category,
                    modifier = Modifier.size(width = 56.dp, height = 72.dp).clip(RoundedCornerShape(12.dp)),
                    letterSize = 24.sp
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "RESERVE THIS BOOK",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.8.sp
                    )
                    Text(book.title, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        "by ${book.author}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(InputValidator.MAX_NAME) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                label = { Text("Your name") },
                isError = name.isNotEmpty() && error != null,
                supportingText = { if (name.isNotEmpty() && error != null) Text(error) }
            )

            Text("Rental duration", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RentalPolicy.durationOptionsDays.forEach { option ->
                    DurationOption(
                        days = option,
                        dueDate = formatDate(now + option * RentalPolicy.DAY_MILLIS),
                        selected = days == option,
                        onClick = { days = option },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Button(
                onClick = {
                    valid?.let { onConfirm(it.value, days) }
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                },
                enabled = valid != null,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Reserve", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun DurationOption(
    days: Int,
    dueDate: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier.clip(shape).clickable(onClick = onClick),
        shape = shape,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$days", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("days", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Due $dueDate",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AddBookDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, author: String, category: String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var author by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }

    val allValid =
        InputValidator.validateRequired(title, "Title", InputValidator.MAX_TITLE) is ValidationResult.Valid &&
            InputValidator.validateRequired(author, "Author", InputValidator.MAX_AUTHOR) is ValidationResult.Valid &&
            InputValidator.validateRequired(category, "Category", InputValidator.MAX_CATEGORY) is ValidationResult.Valid

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Add a book") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(InputValidator.MAX_TITLE) },
                    label = { Text("Title") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it.take(InputValidator.MAX_AUTHOR) },
                    label = { Text("Author") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it.take(InputValidator.MAX_CATEGORY) },
                    label = { Text("Category") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title, author, category) }, enabled = allValid) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
