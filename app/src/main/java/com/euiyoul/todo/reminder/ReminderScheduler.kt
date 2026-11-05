package com.euiyoul.todo.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.euiyoul.todo.R
import com.euiyoul.todo.data.TodoEntity

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /** 할 일 상태에 맞게 알람을 걸거나 취소한다. */
    fun sync(todo: TodoEntity) {
        val due = todo.dueAt
        if (!todo.reminder || todo.isDone || due == null || due <= System.currentTimeMillis()) {
            cancel(todo.id)
            return
        }
        val pi = pendingIntent(todo.id, PendingIntent.FLAG_UPDATE_CURRENT)!!
        if (canScheduleExact(context)) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, due, pi)
        } else {
            // 정확한 알람 권한이 없으면 몇 분 늦어질 수 있는 알람으로 대체
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, due, pi)
        }
    }

    fun cancel(todoId: Long) {
        pendingIntent(todoId, PendingIntent.FLAG_NO_CREATE)?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    private fun pendingIntent(todoId: Long, flag: Int): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java)
            .setAction(ReminderReceiver.ACTION_SHOW)
            .putExtra(ReminderReceiver.EXTRA_TODO_ID, todoId)
        return PendingIntent.getBroadcast(
            context, todoId.toInt(), intent, flag or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val CHANNEL_ID = "todo_reminder"

        fun canScheduleExact(context: Context): Boolean =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

        fun createChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.reminder_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            )
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}
