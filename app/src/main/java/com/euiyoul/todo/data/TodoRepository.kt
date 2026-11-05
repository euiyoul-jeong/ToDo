package com.euiyoul.todo.data

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.room.withTransaction
import com.euiyoul.todo.reminder.ReminderScheduler
import com.euiyoul.todo.widget.TodoWidget
import kotlinx.coroutines.flow.Flow

/**
 * 할 일/카테고리 변경의 단일 진입점.
 * 할 일이 바뀔 때마다 알림을 다시 예약하고 홈 화면 위젯을 갱신한다.
 */
class TodoRepository(
    private val context: Context,
    private val db: AppDatabase,
    private val scheduler: ReminderScheduler,
) {
    private val todoDao = db.todoDao()
    private val categoryDao = db.categoryDao()

    val todos: Flow<List<TodoEntity>> = todoDao.observeAll()
    val categories: Flow<List<CategoryEntity>> = categoryDao.observeAll()

    suspend fun getTodo(id: Long): TodoEntity? = todoDao.getById(id)

    suspend fun getActiveTodos(): List<TodoEntity> = todoDao.getActive()

    suspend fun getCategories(): List<CategoryEntity> = categoryDao.getAll()

    suspend fun save(todo: TodoEntity): Long {
        val id = if (todo.id == 0L) {
            todoDao.insert(todo)
        } else {
            todoDao.update(todo)
            todo.id
        }
        scheduler.sync(todo.copy(id = id))
        refreshWidget()
        return id
    }

    suspend fun setDone(id: Long, done: Boolean) {
        todoDao.setDone(id, done, if (done) System.currentTimeMillis() else null)
        todoDao.getById(id)?.let { scheduler.sync(it) }
        refreshWidget()
    }

    suspend fun delete(id: Long) {
        scheduler.cancel(id)
        todoDao.delete(id)
        refreshWidget()
    }

    /** 삭제 취소용: 지운 항목을 같은 id로 되살린다. */
    suspend fun restore(todo: TodoEntity) {
        todoDao.insert(todo)
        scheduler.sync(todo)
        refreshWidget()
    }

    suspend fun deleteCompleted() {
        todoDao.deleteCompleted()
        refreshWidget()
    }

    suspend fun addCategory(name: String, color: Long) {
        categoryDao.insert(CategoryEntity(name = name, color = color))
        refreshWidget()
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.update(category)
        refreshWidget()
    }

    /** 카테고리를 지우면 그 카테고리의 할 일은 '카테고리 없음'이 된다. */
    suspend fun deleteCategory(id: Long) {
        db.withTransaction {
            todoDao.clearCategory(id)
            categoryDao.delete(id)
        }
        refreshWidget()
    }

    /** 재부팅 등으로 사라진 알람을 모두 다시 건다. */
    suspend fun rescheduleAll() {
        todoDao.getActive().forEach { scheduler.sync(it) }
    }

    suspend fun refreshWidget() {
        TodoWidget().updateAll(context)
    }
}
