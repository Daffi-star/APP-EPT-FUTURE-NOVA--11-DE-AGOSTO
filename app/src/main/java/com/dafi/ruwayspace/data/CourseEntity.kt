package com.dafi.ruwayspace.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses_table")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val progress: Int,
    val colorHex: String = "#2196F3",
    val status: String = "Sin empezar", // "Sin empezar", "En progreso", "Completado"
    val difficulty: String = "Fácil",   // "Fácil", "Medio", "Difícil"
    // En CourseEntity.kt
    val pdfUris: List<String> = emptyList() // Inicializa vacía        // Ruta o URI del PDF adjunto
)