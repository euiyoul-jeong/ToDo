package com.euiyoul.todo.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.euiyoul.todo.repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** 재부팅·앱 업데이트·정확한 알람 권한 변경 시 알람을 다시 건다. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                context.repository.rescheduleAll()
                context.repository.refreshWidget()
            } finally {
                pending.finish()
            }
        }
    }
}
