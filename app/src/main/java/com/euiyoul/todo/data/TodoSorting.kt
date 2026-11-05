package com.euiyoul.todo.data

import androidx.annotation.StringRes
import com.euiyoul.todo.R

enum class SortOrder(@param:StringRes val labelRes: Int) {
    DUE(R.string.sort_due),
    PRIORITY(R.string.sort_priority),
}

/**
 * 미완료 할 일 정렬.
 * - [SortOrder.DUE]: 마감일 빠른 순(지난 것 포함) → 마감 없음, 같으면 중요도 높은 순
 * - [SortOrder.PRIORITY]: 중요도 높은 순, 같으면 마감일 빠른 순 → 마감 없음
 * 모든 조건이 같으면 먼저 만든 것이 앞에 온다.
 */
fun List<TodoEntity>.sortedActive(order: SortOrder = SortOrder.DUE): List<TodoEntity> {
    val byDue = compareBy<TodoEntity, Long?>(nullsLast()) { it.dueAt }
    val byPriority = compareBy<TodoEntity> { it.priority.rank }
    val comparator = when (order) {
        SortOrder.DUE -> byDue.then(byPriority)
        SortOrder.PRIORITY -> byPriority.then(byDue)
    }.thenBy { it.createdAt }
    return sortedWith(comparator)
}

/** 완료된 할 일은 최근에 완료한 것이 위로. */
fun List<TodoEntity>.sortedCompleted(): List<TodoEntity> =
    sortedByDescending { it.completedAt ?: 0L }

fun TodoEntity.isOverdue(now: Long = System.currentTimeMillis()): Boolean =
    !isDone && dueAt != null && dueAt < now
