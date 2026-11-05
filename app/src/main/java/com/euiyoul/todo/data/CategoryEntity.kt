package com.euiyoul.todo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** ARGB 색상 값 */
    val color: Long,
)

/** 카테고리에 고를 수 있는 색상 팔레트 */
val CategoryColors: List<Long> = listOf(
    0xFF1E88E5, // 파랑
    0xFF43A047, // 초록
    0xFFFB8C00, // 주황
    0xFF8E24AA, // 보라
    0xFFE53935, // 빨강
    0xFF00ACC1, // 청록
    0xFF6D4C41, // 갈색
    0xFF546E7A, // 회색
)
