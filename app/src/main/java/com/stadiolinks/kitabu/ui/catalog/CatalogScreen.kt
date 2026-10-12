package com.stadiolinks.kitabu.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stadiolinks.kitabu.data.local.BookEntity
import com.stadiolinks.kitabu.domain.AvailabilityFilter
import com.stadiolinks.kitabu.domain.InputValidator
import com.stadiolinks.kitabu.domain.RentalPolicy
import com.stadiolinks.kitabu.domain.ValidationResult
import com.stadiolinks.kitabu.ui.components.EmptyState
import com.stadiolinks.kitabu.ui.components.NoInsets
import com.stadiolinks.kitabu.ui.components.StatusBadge
import com.stadiolinks.kitabu.ui.theme.AvailableGreen
import com.stadiolinks.kitabu.ui.theme.BorrowedRed
import kotlinx.coroutines.launch

/** Stateless: everything comes in as parameters and every action goes out as a callback (state hoisting). */
@OptIn(ExperimentalMaterial3Api::class)
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

    Column(modifier = modifier) {
        TopAppBar(
            title = { Text("Kitabu", fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add book")
                }
            },
            windowInsets = NoInsets
        )

        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            placeholder = { Text("Search by title or author") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (uiState.query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            }
        )

        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AvailabilityFilter.entries.forEach { option ->
                FilterChip(
                    selected = uiState.filter == option,
                    onClick = { onFilterChange(option) },
                    label = { Text(option.label) }
                )
            }
        }

        if (uiState.books.isEmpty()) {
            EmptyState("No books found. Try a different search or filter.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
    Card(
        onClick = onClick,
        enabled = book.isAvailable, // only available books can be reserved
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = book.category,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Reserve this book", style = MaterialTheme.typography.titleLarge)
            Text(book.title, style = MaterialTheme.typography.titleMedium)
            Text("by ${book.author}", style = MaterialTheme.typography.bodyMedium)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(InputValidator.MAX_NAME) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Your name") },
                isError = name.isNotEmpty() && error != null,
                supportingText = { if (name.isNotEmpty() && error != null) Text(error) }
            )

            Text("Rental duration", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RentalPolicy.durationOptionsDays.forEach { option ->
                    FilterChip(
                        selected = days == option,
                        onClick = { days = option },
                        label = { Text("$option days") }
                    )
                }
            }

            Button(
                onClick = {
                    valid?.let { onConfirm(it.value, days) }
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                },
                enabled = valid != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reserve")
            }
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
        title = { Text("Add a book") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(InputValidator.MAX_TITLE) },
                    label = { Text("Title") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it.take(InputValidator.MAX_AUTHOR) },
                    label = { Text("Author") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it.take(InputValidator.MAX_CATEGORY) },
                    label = { Text("Category") },
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
