package com.euiyoul.todo

import android.app.Application
import android.content.Context
import com.euiyoul.todo.data.AppDatabase
import com.euiyoul.todo.data.TodoRepository
import com.euiyoul.todo.reminder.ReminderScheduler

class ToDoApp : Application() {
    val repository: TodoRepository by lazy {
        TodoRepository(this, AppDatabase.build(this), ReminderScheduler(this))
    }

    override fun onCreate() {
        super.onCreate()
        ReminderScheduler.createChannel(this)
    }
}

val Context.repository: TodoRepository
    get() = (applicationContext as ToDoApp).repository
