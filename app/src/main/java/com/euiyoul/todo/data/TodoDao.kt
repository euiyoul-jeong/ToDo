package com.euiyoul.todo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos")
    fun observeAll(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE isDone = 0")
    suspend fun getActive(): List<TodoEntity>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getById(id: Long): TodoEntity?

    @Insert
    suspend fun insert(todo: TodoEntity): Long

    @Update
    suspend fun update(todo: TodoEntity)

    @Query("UPDATE todos SET isDone = :done, completedAt = :completedAt WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean, completedAt: Long?)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM todos WHERE isDone = 1")
    suspend fun deleteCompleted()

    @Query("UPDATE todos SET categoryId = NULL WHERE categoryId = :categoryId")
    suspend fun clearCategory(categoryId: Long)
}
