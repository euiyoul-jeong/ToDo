package com.euiyoul.todo.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euiyoul.todo.data.CategoryEntity
import com.euiyoul.todo.data.SortOrder
import com.euiyoul.todo.data.TodoEntity
import com.euiyoul.todo.data.TodoRepository
import com.euiyoul.todo.data.sortedActive
import com.euiyoul.todo.data.sortedCompleted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodoListUiState(
    val active: List<TodoEntity> = emptyList(),
    val completed: List<TodoEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    /** null이면 전체 */
    val selectedCategoryId: Long? = null,
    val sortOrder: SortOrder = SortOrder.DUE,
    val loaded: Boolean = false,
)

class TodoListViewModel(private val repository: TodoRepository) : ViewModel() {
    private val selectedCategoryId = MutableStateFlow<Long?>(null)
    private val sortOrder = MutableStateFlow(SortOrder.DUE)

    val uiState: StateFlow<TodoListUiState> = combine(
        repository.todos, repository.categories, selectedCategoryId, sortOrder,
    ) { todos, categories, selected, sort ->
        // 선택한 카테고리가 삭제됐으면 전체로
        val filterId = selected?.takeIf { id -> categories.any { it.id == id } }
        val filtered = if (filterId == null) todos else todos.filter { it.categoryId == filterId }
        TodoListUiState(
            active = filtered.filter { !it.isDone }.sortedActive(sort),
            completed = filtered.filter { it.isDone }.sortedCompleted(),
            categories = categories,
            selectedCategoryId = filterId,
            sortOrder = sort,
            loaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodoListUiState())

    fun selectCategory(id: Long?) {
        selectedCategoryId.value = id
    }

    fun setSortOrder(order: SortOrder) {
        sortOrder.value = order
    }

    fun setDone(id: Long, done: Boolean) = viewModelScope.launch { repository.setDone(id, done) }

    fun delete(todo: TodoEntity) = viewModelScope.launch { repository.delete(todo.id) }

    fun restore(todo: TodoEntity) = viewModelScope.launch { repository.restore(todo) }

    fun deleteCompleted() = viewModelScope.launch { repository.deleteCompleted() }

    fun addCategory(name: String, color: Long) = viewModelScope.launch { repository.addCategory(name, color) }

    fun updateCategory(category: CategoryEntity) = viewModelScope.launch { repository.updateCategory(category) }

    fun deleteCategory(id: Long) = viewModelScope.launch { repository.deleteCategory(id) }
}
