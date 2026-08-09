package com.dafi.ruwayspace.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.dafi.ruwayspace.model.SubTask


@Entity(tableName = "topics")
data class Topic(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var emoji: String,
    var title: String,
    var category: String,
    var status: String,
    var date: String = "",
    @TypeConverters(Converters::class)
    var subtasks: MutableList<SubTask>
) {
    val progress: Int
        get() {
            if (subtasks.isEmpty()) return 0
            val completed = subtasks.count { it.isCompleted }
            return (completed * 100) / subtasks.size
        }
}