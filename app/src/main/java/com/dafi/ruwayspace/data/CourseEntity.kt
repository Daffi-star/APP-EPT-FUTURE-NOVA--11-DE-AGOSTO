package com.dafi.ruwayspace.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses_table")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val progress: Int,
    val status: String,
    val difficulty: String,
    val pdfUris: List<String> = emptyList(),
    val notes: String = "",        // Idea 2: Apuntes rápidos
    val dueDate: String = "",      // Idea 1: Fecha límite o examen (ej. "20/12/2026")
    val categoryTag: String = "Teoría" // Idea 4: Categoría libre ("Teoría", "Práctica", "Simulacro")
)