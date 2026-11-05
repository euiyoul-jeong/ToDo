package com.euiyoul.todo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TodoSortingTest {
    private fun todo(id: Long, due: Long?, priority: Priority, createdAt: Long = id) =
        TodoEntity(id = id, title = "t$id", dueAt = due, priority = priority, createdAt = createdAt)

    private val items = listOf(
        todo(1, due = null, priority = Priority.HIGH),
        todo(2, due = 300, priority = Priority.LOW),
        todo(3, due = 100, priority = Priority.NORMAL),
        todo(4, due = 300, priority = Priority.HIGH),
        todo(5, due = null, priority = Priority.LOW),
    )

    @Test
    fun dueOrder_earliestFirst_noDueLast_tieBrokenByPriority() {
        val ids = items.sortedActive(SortOrder.DUE).map { it.id }
        assertEquals(listOf(3L, 4L, 2L, 1L, 5L), ids)
    }

    @Test
    fun priorityOrder_highFirst_tieBrokenByDue() {
        val ids = items.sortedActive(SortOrder.PRIORITY).map { it.id }
        assertEquals(listOf(4L, 1L, 3L, 2L, 5L), ids)
    }

    @Test
    fun sameDueAndPriority_olderFirst() {
        val list = listOf(
            todo(1, due = 100, priority = Priority.NORMAL, createdAt = 20),
            todo(2, due = 100, priority = Priority.NORMAL, createdAt = 10),
        )
        assertEquals(listOf(2L, 1L), list.sortedActive().map { it.id })
    }

    @Test
    fun completed_mostRecentFirst() {
        val list = listOf(
            TodoEntity(id = 1, title = "a", isDone = true, completedAt = 10),
            TodoEntity(id = 2, title = "b", isDone = true, completedAt = 30),
            TodoEntity(id = 3, title = "c", isDone = true, completedAt = 20),
        )
        assertEquals(listOf(2L, 3L, 1L), list.sortedCompleted().map { it.id })
    }

    @Test
    fun overdue_onlyWhenPastDueAndNotDone() {
        assertTrue(TodoEntity(title = "a", dueAt = 100).isOverdue(now = 200))
        assertFalse(TodoEntity(title = "a", dueAt = 300).isOverdue(now = 200))
        assertFalse(TodoEntity(title = "a", dueAt = 100, isDone = true).isOverdue(now = 200))
        assertFalse(TodoEntity(title = "a", dueAt = null).isOverdue(now = 200))
    }
}
