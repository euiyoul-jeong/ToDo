package com.euiyoul.todo.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "todos",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        )
    ],
    indices = [Index("categoryId")],
)
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val memo: String = "",
    /** 마감 일시 (epoch millis). null이면 마감 없음. */
    val dueAt: Long? = null,
    /** true이면 [dueAt] 시각에 알림을 띄운다. */
    val reminder: Boolean = false,
    val priority: Priority = Priority.NORMAL,
    val categoryId: Long? = null,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
)
