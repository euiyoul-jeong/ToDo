package com.euiyoul.todo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

class Converters {
    @TypeConverter
    fun fromPriority(value: Priority): String = value.name

    @TypeConverter
    fun toPriority(value: String): Priority = Priority.valueOf(value)
}

@Database(entities = [TodoEntity::class, CategoryEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "todo.db")
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // 처음 설치 시 기본 카테고리
                        db.execSQL("INSERT INTO categories (name, color) VALUES ('업무', ${CategoryColors[0]})")
                        db.execSQL("INSERT INTO categories (name, color) VALUES ('개인', ${CategoryColors[1]})")
                    }
                })
                .build()
    }
}
