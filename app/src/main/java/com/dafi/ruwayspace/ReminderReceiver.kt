package com.dafi.ruwayspace

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.dafi.ruwayspace.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra("REMINDER_ID", -1)
        val title = intent.getStringExtra("REMINDER_TITLE") ?: "Recordatorio"

        // 1. Mostrar la notificación flotante en el sistema
        mostrarNotificacion(context, reminderId, title)

        // 2. Actualizar la BD a "Completado" automáticamente
        if (reminderId != -1) {
            val db = AppDatabase.getDatabase(context)
            CoroutineScope(Dispatchers.IO).launch {
                db.reminderDao().updateStatus(reminderId, "Completado")
            }
        }
    }

    private fun mostrarNotificacion(context: Context, id: Int, title: String) {
        val channelId = "ruway_space_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear el canal de notificación (Obligatorio para Android 8.0 en adelante)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Avisos y Alarmas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para las alarmas y recordatorios de RuwaySpace"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Construir la notificación
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Icono por defecto de alarma de Android (puedes cambiarlo luego por uno tuyo)
            .setContentTitle("¡Es hora de tu aviso!")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Lanzar la notificación
        notificationManager.notify(id, notification)
    }
}