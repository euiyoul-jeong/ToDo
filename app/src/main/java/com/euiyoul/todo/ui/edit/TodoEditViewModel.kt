package com.euiyoul.todo.ui.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euiyoul.todo.data.CategoryEntity
import com.euiyoul.todo.data.Priority
import com.euiyoul.todo.data.TodoEntity
import com.euiyoul.todo.data.TodoRepository
import com.euiyoul.todo.ui.Routes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoEditViewModel(
    private val repository: TodoRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val todoId: Long = savedStateHandle[Routes.ARG_ID] ?: Routes.NEW_ID
    val isNew: Boolean get() = todoId == Routes.NEW_ID

    var title by mutableStateOf("")
    var memo by mutableStateOf("")
    var dueAt by mutableStateOf<Long?>(null)
    var reminder by mutableStateOf(false)
    var priority by mutableStateOf(Priority.NORMAL)
    var categoryId by mutableStateOf<Long?>(null)

    /** 수정 중인 원본 (완료 여부 등 유지용) */
    private var original: TodoEntity? = null
    var loaded by mutableStateOf(isNew)
        private set

    val categories: StateFlow<List<CategoryEntity>> =
        repository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        if (!isNew) {
            viewModelScope.launch {
                repository.getTodo(todoId)?.let { todo ->
                    original = todo
                    title = todo.title
                    memo = todo.memo
                    dueAt = todo.dueAt
                    reminder = todo.reminder
                    priority = todo.priority
                    categoryId = todo.categoryId
                }
                loaded = true
            }
        }
    }

    val canSave: Boolean get() = title.isNotBlank()

    fun updateDue(value: Long?) {
        dueAt = value
        if (value == null) reminder = false
    }

    fun save(onSaved: () -> Unit) {
        if (!canSave) return
        val base = original ?: TodoEntity(title = "")
        val todo = base.copy(
            title = title.trim(),
            memo = memo.trim(),
            dueAt = dueAt,
            reminder = reminder && dueAt != null,
            priority = priority,
            categoryId = categoryId,
        )
        viewModelScope.launch {
            repository.save(todo)
            onSaved()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        if (isNew) return
        viewModelScope.launch {
            repository.delete(todoId)
            onDeleted()
        }
    }
}
