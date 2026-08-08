package com.dafi.ruwayspace

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dafi.ruwayspace.data.ChatDao
import com.dafi.ruwayspace.data.ChatMessageEntity
import com.dafi.ruwayspace.data.CourseDao
import com.dafi.ruwayspace.data.CourseEntity

@Database(entities = [ChatMessageEntity::class, CourseEntity::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class) // <--- Agrega esto
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun courseDao(): CourseDao // 2. Agrega el DAO aquí

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
                .fallbackToDestructiveMigration() // 3. Esto evita errores al cambiar la estructura
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}