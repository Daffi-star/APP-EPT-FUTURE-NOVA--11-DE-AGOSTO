package com.dafi.ruwayspace.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subtitle: String,      // Ej: "Aula 205" o "Laboratorio 1"
    val time: String,          // Ej: "10:00 AM"
    val date: String,          // Formato "dd/MM/yyyy" para saber si es hoy o próximo
    val timeRemaining: String, // Ej: "En 2h" o "En 2 días"
    val category: String,      // "Examen", "Práctica", "Tarea", etc.
    val status: String         // "Pendiente" o "Completado"
)