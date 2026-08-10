package com.dafi.ruwayspace.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ReminderDao {

    // Cambiado de reminder_table a reminders
    @Query("SELECT * FROM reminders")
    suspend fun getAllReminders(): List<ReminderEntity>

    @Insert
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    // Cambiado de reminder_table a reminders
    @Query("UPDATE reminders SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    // NUEVO: Método para eliminar un recordatorio por su ID
    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: Int)

    // En ReminderDao.kt
    @Query("DELETE FROM reminders WHERE status = 'Completado'")
    suspend fun deleteCompleted()

    // (Bonus) Por si quieres un botón para borrar TODO sin importar el estado
    @Query("DELETE FROM reminders")
    suspend fun deleteAll()

    // Agrega esta línea para contar cuántos recordatorios hay guardados
    @Query("SELECT COUNT(*) FROM reminders") // (Verifica si el nombre de tu tabla en la entidad es reminder_entity o similar)
    suspend fun getReminderCount(): Int

    // Cambia la consulta para que filtre por la fecha de hoy
    @Query("SELECT COUNT(*) FROM reminders WHERE date = :fechaHoy")
    suspend fun getRemindersCountForToday(fechaHoy: String): Int
}