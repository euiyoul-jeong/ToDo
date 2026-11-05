package com.euiyoul.todo.reminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.euiyoul.todo.R
import com.euiyoul.todo.repository
import com.euiyoul.todo.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** 마감 알림 표시와, 알림의 "완료" 버튼 처리를 맡는다. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_TODO_ID, -1L)
        if (id < 0) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_SHOW -> show(context, id)
                    ACTION_DONE -> {
                        context.repository.setDone(id, true)
                        NotificationManagerCompat.from(context).cancel(id.toInt())
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    @SuppressLint("MissingPermission") // hasNotificationPermission()으로 확인함
    private suspend fun show(context: Context, id: Long) {
        val todo = context.repository.getTodo(id) ?: return
        if (todo.isDone || !hasNotificationPermission(context)) return

        val openIntent = PendingIntent.getActivity(
            context, id.toInt(),
            MainActivity.editIntent(context, id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val doneIntent = PendingIntent.getBroadcast(
            context, id.toInt(),
            Intent(context, ReminderReceiver::class.java)
                .setAction(ACTION_DONE)
                .putExtra(EXTRA_TODO_ID, id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(todo.title)
            .setContentText(todo.memo.ifBlank { context.getString(R.string.reminder_due_now) })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .addAction(0, context.getString(R.string.reminder_action_done), doneIntent)
            .build()
        NotificationManagerCompat.from(context).notify(id.toInt(), notification)
    }

    companion object {
        const val ACTION_SHOW = "com.euiyoul.todo.action.SHOW_REMINDER"
        const val ACTION_DONE = "com.euiyoul.todo.action.MARK_DONE"
        const val EXTRA_TODO_ID = "todo_id"

        fun hasNotificationPermission(context: Context): Boolean =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }
}
