package com.euiyoul.todo.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.euiyoul.todo.R
import com.euiyoul.todo.data.CategoryEntity
import com.euiyoul.todo.data.SortOrder
import com.euiyoul.todo.data.TodoEntity
import com.euiyoul.todo.data.isOverdue
import com.euiyoul.todo.repository
import com.euiyoul.todo.ui.category.CategoryDialog
import com.euiyoul.todo.util.formatDue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
) {
    val repository = LocalContext.current.repository
    val viewModel: TodoListViewModel = viewModel { TodoListViewModel(repository) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var menuOpen by remember { mutableStateOf(false) }
    var showCategoryDialog by rememberSaveable { mutableStateOf(false) }
    var showCompleted by rememberSaveable { mutableStateOf(false) }
    val categoriesById = state.categories.associateBy { it.id }

    val deletedMessage = stringResource(R.string.deleted)
    val undoLabel = stringResource(R.string.undo)
    val onDelete: (TodoEntity) -> Unit = { todo ->
        viewModel.delete(todo)
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                deletedMessage, actionLabel = undoLabel, duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.restore(todo)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        SortOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = { Text(stringResource(order.labelRes)) },
                                leadingIcon = {
                                    RadioButton(selected = state.sortOrder == order, onClick = null)
                                },
                                onClick = {
                                    viewModel.setSortOrder(order)
                                    menuOpen = false
                                },
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.manage_categories)) },
                            onClick = {
                                showCategoryDialog = true
                                menuOpen = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete_completed)) },
                            enabled = state.completed.isNotEmpty(),
                            onClick = {
                                viewModel.deleteCompleted()
                                menuOpen = false
                            },
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_todo))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            CategoryFilterRow(
                categories = state.categories,
                selectedId = state.selectedCategoryId,
                onSelect = viewModel::selectCategory,
            )

            if (state.loaded && state.active.isEmpty() && state.completed.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.empty_list),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                return@Column
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp),
            ) {
                items(state.active, key = { it.id }) { todo ->
                    SwipeToDelete(onDelete = { onDelete(todo) }) {
                        TodoRow(
                            todo = todo,
                            category = todo.categoryId?.let { categoriesById[it] },
                            onCheckedChange = { viewModel.setDone(todo.id, it) },
                            onClick = { onEdit(todo.id) },
                        )
                    }
                }
                if (state.completed.isNotEmpty()) {
                    item(key = "completed_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCompleted = !showCompleted }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                stringResource(R.string.completed_header, state.completed.size),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                if (showCompleted) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                            )
                        }
                    }
                    if (showCompleted) {
                        items(state.completed, key = { it.id }) { todo ->
                            SwipeToDelete(onDelete = { onDelete(todo) }) {
                                TodoRow(
                                    todo = todo,
                                    category = todo.categoryId?.let { categoriesById[it] },
                                    onCheckedChange = { viewModel.setDone(todo.id, it) },
                                    onClick = { onEdit(todo.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCategoryDialog) {
        CategoryDialog(
            categories = state.categories,
            onAdd = viewModel::addCategory,
            onUpdate = viewModel::updateCategory,
            onDelete = viewModel::deleteCategory,
            onDismiss = { showCategoryDialog = false },
        )
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<CategoryEntity>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = selectedId == null,
                onClick = { onSelect(null) },
                label = { Text(stringResource(R.string.filter_all)) },
            )
        }
        items(categories, key = { it.id }) { category ->
            FilterChip(
                selected = selectedId == category.id,
                onClick = { onSelect(category.id) },
                label = { Text(category.name) },
                leadingIcon = { ColorDot(Color(category.color)) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDelete(onDelete: () -> Unit, content: @Composable () -> Unit) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it != SwipeToDismissBoxValue.Settled) onDelete()
            it != SwipeToDismissBoxValue.Settled
        },
    )
    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = if (state.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                    Alignment.CenterStart
                } else {
                    Alignment.CenterEnd
                },
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        content()
    }
}

@Composable
private fun TodoRow(
    todo: TodoEntity,
    category: CategoryEntity?,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = todo.isDone, onCheckedChange = onCheckedChange)
        Column(Modifier.weight(1f).padding(vertical = 4.dp)) {
            Text(
                todo.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (todo.isDone) TextDecoration.LineThrough else null,
                color = if (todo.isDone) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
            )
            if (todo.dueAt != null || category != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (todo.dueAt != null) {
                        val overdue = todo.isOverdue()
                        val color = if (overdue) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                        if (todo.reminder && !todo.isDone) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = stringResource(R.string.reminder),
                                tint = color,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(Modifier.width(2.dp))
                        }
                        Text(formatDue(todo.dueAt), style = MaterialTheme.typography.bodySmall, color = color)
                    }
                    if (todo.dueAt != null && category != null) Spacer(Modifier.width(8.dp))
                    if (category != null) {
                        ColorDot(Color(category.color), size = 8)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            category.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        PriorityBadge(todo)
    }
}

@Composable
private fun PriorityBadge(todo: TodoEntity) {
    val color = Color(todo.priority.color).copy(alpha = if (todo.isDone) 0.4f else 1f)
    Text(
        stringResource(todo.priority.labelRes),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
fun ColorDot(color: Color, size: Int = 10) {
    Box(
        Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color)
    )
}
