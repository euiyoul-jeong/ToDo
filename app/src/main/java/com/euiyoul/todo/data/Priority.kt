package com.euiyoul.todo.data

import androidx.annotation.StringRes
import com.euiyoul.todo.R

/** 정렬 시 [rank]가 작을수록 앞에 온다. */
enum class Priority(val rank: Int, @param:StringRes val labelRes: Int, val color: Long) {
    HIGH(0, R.string.priority_high, 0xFFE53935),
    NORMAL(1, R.string.priority_normal, 0xFF1E88E5),
    LOW(2, R.string.priority_low, 0xFF9E9E9E),
}
