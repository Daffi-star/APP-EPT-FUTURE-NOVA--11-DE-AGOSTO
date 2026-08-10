package com.dafi.ruwayspace

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dafi.ruwayspace.data.ReminderEntity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

class AvisosActivity : AppCompatActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private var filtroActual = "Todos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_avisos)

        findViewById<ImageView>(R.id.btnSettings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Pide permiso de notificaciones en tiempo de ejecución (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // Botón flotante para abrir diálogo de nueva alarma (vacío)
        findViewById<FloatingActionButton>(R.id.fabAddReminder).setOnClickListener {
            mostrarDialogoAgregar()
        }

        configurarFiltros()
        cargarRecordatorios()
    }

    private fun configurarFiltros() {
        val chipTodos = findViewById<TextView>(R.id.chipTodos)
        val chipHoy = findViewById<TextView>(R.id.chipHoy)
        val chipPendientes = findViewById<TextView>(R.id.chipPendientes)
        val chipCompletados = findViewById<TextView>(R.id.chipCompletados)

        val chips = listOf(chipTodos, chipHoy, chipPendientes, chipCompletados)

        fun actualizarEstilos(seleccionado: TextView) {
            chips.forEach { chip ->
                if (chip == seleccionado) {
                    chip.setBackgroundResource(R.drawable.bg_chip_selected)
                    chip.setTextColor(resources.getColor(android.R.color.white, theme))
                } else {
                    chip.setBackgroundResource(R.drawable.bg_chip_unselected)
                    chip.setTextColor(resources.getColor(android.R.color.darker_gray, theme))
                }
            }
        }

        chipTodos.setOnClickListener { filtroActual = "Todos"; actualizarEstilos(it as TextView); cargarRecordatorios() }
        chipHoy.setOnClickListener { filtroActual = "Hoy"; actualizarEstilos(it as TextView); cargarRecordatorios() }
        chipPendientes.setOnClickListener { filtroActual = "Pendientes"; actualizarEstilos(it as TextView); cargarRecordatorios() }
        chipCompletados.setOnClickListener { filtroActual = "Completados"; actualizarEstilos(it as TextView); cargarRecordatorios() }
    }

    private fun cargarRecordatorios() {
        lifecycleScope.launch(Dispatchers.IO) {
            val lista = database.reminderDao().getAllReminders()
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaHoy = sdf.format(Date())

            val filtrados = when (filtroActual) {
                "Hoy" -> lista.filter { it.date.trim().equals(fechaHoy, ignoreCase = true) }
                "Pendientes" -> lista.filter { it.status.equals("Pendiente", ignoreCase = true) }
                "Completados" -> lista.filter { it.status.equals("Completado", ignoreCase = true) }
                else -> lista
            }

            withContext(Dispatchers.Main) {
                mostrarEnPantalla(filtrados)
            }
        }
    }

    private fun mostrarEnPantalla(reminders: List<ReminderEntity>) {
        val container = findViewById<LinearLayout>(R.id.containerRemindersList)
        container.removeAllViews()

        if (reminders.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No hay recordatorios en esta sección."
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.darker_gray, theme))
                setPadding(16, 32, 16, 16)
            }
            container.addView(emptyTv)
            return
        }

        for (reminder in reminders) {
            val cardView = View.inflate(this, R.layout.item_reminder, null)

            cardView.findViewById<TextView>(R.id.tvReminderTitle).text = reminder.title
            cardView.findViewById<TextView>(R.id.tvReminderSubtitle).text = "${reminder.subtitle} • ${reminder.time}"
            cardView.findViewById<TextView>(R.id.tvReminderBadge).text = reminder.timeRemaining

            // Al hacer clic en la tarjeta, abrimos el diálogo para editar este recordatorio específico
            // Al hacer clic en la tarjeta, mostramos opciones para Editar o Eliminar
            cardView.setOnClickListener {
                val opciones = arrayOf("Editar recordatorio", "Eliminar recordatorio")
                AlertDialog.Builder(this)
                    .setTitle(reminder.title)
                    .setItems(opciones) { _, which ->
                        when (which) {
                            0 -> {
                                // Opción 0: Editar
                                mostrarDialogoAgregar(reminder)
                            }
                            1 -> {
                                // Opción 1: Eliminar
                                eliminarRecordatorio(reminder)
                            }
                        }
                    }
                    .show()
            }

            container.addView(cardView)
        }
    }

    private fun eliminarRecordatorio(reminder: ReminderEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            // 1. Cancelar la alarma activa en el AlarmManager para que no suene
            val intent = Intent(this@AvisosActivity, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this@AvisosActivity,
                reminder.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)

            // 2. Borrar de la base de datos Room
            database.reminderDao().deleteReminder(reminder.id)

            // 3. Recargar la interfaz en el hilo principal
            withContext(Dispatchers.Main) {
                Toast.makeText(this@AvisosActivity, "Recordatorio eliminado", Toast.LENGTH_SHORT).show()
                cargarRecordatorios()
            }
        }
    }

    private fun programarAlarma(reminderId: Int, title: String, timeStr: String) {
        try {
            val parts = timeStr.split(":")
            if (parts.size < 2) return
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val intent = Intent(this, ReminderReceiver::class.java).apply {
                putExtra("REMINDER_ID", reminderId)
                putExtra("REMINDER_TITLE", title)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun mostrarDialogoAgregar(reminderToEdit: ReminderEntity? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_reminder, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etSubtitle = dialogView.findViewById<EditText>(R.id.etSubtitle)
        val etTime = dialogView.findViewById<EditText>(R.id.etTime)
        val etBadge = dialogView.findViewById<EditText>(R.id.etBadge)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveReminder)

        // Si estamos editando, rellenamos los campos con los datos actuales
        reminderToEdit?.let {
            etTitle.setText(it.title)
            etSubtitle.setText(it.subtitle)
            etTime.setText(it.time)
            etBadge.setText(it.timeRemaining)
        }

        // Configurar el selector de hora al hacer clic en el campo etTime
        etTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                    etTime.setText(formattedTime)
                },
                currentHour,
                currentMinute,
                false
            )
            timePickerDialog.show()
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val subtitle = etSubtitle.text.toString().trim()
            val time = etTime.text.toString().trim()
            val badge = etBadge.text.toString().trim()

            if (title.isNotEmpty() && time.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val fechaHoy = sdf.format(Date())

                    val finalId: Int

                    if (reminderToEdit != null) {
                        finalId = reminderToEdit.id
                        val updatedReminder = reminderToEdit.copy(
                            title = title,
                            subtitle = subtitle,
                            time = time,
                            timeRemaining = if (badge.isNotEmpty()) badge else reminderToEdit.timeRemaining
                        )
                        database.reminderDao().updateReminder(updatedReminder)
                    } else {
                        val newReminder = ReminderEntity(
                            title = title,
                            subtitle = subtitle,
                            time = time,
                            date = fechaHoy,
                            timeRemaining = if (badge.isNotEmpty()) badge else "Hoy",
                            category = "General",
                            status = "Pendiente"
                        )
                        // Guardamos y obtenemos el ID generado
                        finalId = database.reminderDao().insertReminder(newReminder).toInt()
                    }

                    // Programamos la alarma nativa del sistema
                    programarAlarma(finalId, title, time)

                    withContext(Dispatchers.Main) {
                        val mensaje = if (reminderToEdit != null) "¡Recordatorio actualizado!" else "¡Alarma programada!"
                        Toast.makeText(this@AvisosActivity, mensaje, Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        cargarRecordatorios()
                    }
                }
            } else {
                Toast.makeText(this, "Escribe un título y selecciona una hora", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun confirmarLimpieza(mensaje: String, tipo: String) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar")
            .setMessage(mensaje)
            .setPositiveButton("Sí, borrar") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    if (tipo == "completed") {
                        database.reminderDao().deleteCompleted()
                    } else {
                        database.reminderDao().deleteAll()
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AvisosActivity, "Limpieza realizada", Toast.LENGTH_SHORT).show()
                        cargarRecordatorios() // Recargamos la lista vacía
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}