package com.dafi.ruwayspace

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dafi.ruwayspace.data.ChatDao
import com.dafi.ruwayspace.data.ChatMessageEntity
import com.dafi.ruwayspace.data.Converters
import com.dafi.ruwayspace.data.CourseDao
import com.dafi.ruwayspace.data.CourseEntity
import com.dafi.ruwayspace.data.ExamDao
import com.dafi.ruwayspace.data.ExamEntity
import com.dafi.ruwayspace.data.Topic
import com.dafi.ruwayspace.data.TopicDao

@Database(entities = [ChatMessageEntity::class, CourseEntity::class, Topic::class, ExamEntity::class], version = 9, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun courseDao(): CourseDao
    abstract fun topicDao(): TopicDao
    abstract fun examDao(): ExamDao // <--- ¡Añadido aquí!

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ruwayspace_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}