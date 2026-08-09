package com.dafi.ruwayspace.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exams_table")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var title: String,         // "Examen de Matemáticas"
    var subject: String,       // "Matemáticas"
    var classroom: String,     // "Aula 203"
    var date: String,          // "22 de mayo"
    var daysRemaining: String, // "En 5 días"
    var iconEmoji: String = "📊", // Emoji elegido por el usuario
    var cardColor: String = "#E3F2FD" // Color pastel de fondo
)