package com.euiyoul.todo.ui.category

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import com.euiyoul.todo.R
import com.euiyoul.todo.data.CategoryColors
import com.euiyoul.todo.data.CategoryEntity
import com.euiyoul.todo.ui.list.ColorDot

/** 카테고리 추가·이름/색 변경·삭제 */
@Composable
fun CategoryDialog(
    categories: List<CategoryEntity>,
    onAdd: (name: String, color: Long) -> Unit,
    onUpdate: (CategoryEntity) -> Unit,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var name by rememberSaveable { mutableStateOf("") }
    var color by rememberSaveable { mutableLongStateOf(CategoryColors.first()) }
    var pendingDelete by rememberSaveable { mutableStateOf<Long?>(null) }

    fun resetForm() {
        editingId = null
        name = ""
        color = CategoryColors[categories.size % CategoryColors.size]
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.manage_categories)) },
        text = {
            Column {
                LazyColumn(Modifier.heightIn(max = 240.dp)) {
                    items(categories, key = { it.id }) { category ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ColorDot(Color(category.color), size = 12)
                            Spacer(Modifier.width(12.dp))
                            Text(category.name, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                editingId = category.id
                                name = category.name
                                color = category.color
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                            }
                            IconButton(onClick = { pendingDelete = category.id }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                            }
                        }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text(stringResource(if (editingId == null) R.string.new_category else R.string.edit_category))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.size(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CategoryColors.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .then(
                                    if (c == color) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable { color = c },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (c == color) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (editingId != null) {
                        TextButton(onClick = { resetForm() }) { Text(stringResource(R.string.cancel)) }
                    }
                    TextButton(
                        enabled = name.isNotBlank(),
                        onClick = {
                            val id = editingId
                            if (id == null) {
                                onAdd(name.trim(), color)
                            } else {
                                onUpdate(CategoryEntity(id = id, name = name.trim(), color = color))
                            }
                            resetForm()
                        },
                    ) {
                        Text(stringResource(if (editingId == null) R.string.add else R.string.save))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        },
    )

    pendingDelete?.let { id ->
        val target = categories.firstOrNull { it.id == id }
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.delete_category_title, target?.name.orEmpty())) },
            text = { Text(stringResource(R.string.delete_category_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(id)
                    if (editingId == id) resetForm()
                    pendingDelete = null
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}
