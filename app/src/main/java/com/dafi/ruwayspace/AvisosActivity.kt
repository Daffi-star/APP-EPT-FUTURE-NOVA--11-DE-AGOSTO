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

class AvisosActivity : AppCompatActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private var filtroActual = "Todos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_avisos)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // Botón flotante para abrir diálogo de nueva alarma
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

            container.addView(cardView)
        }
    }

    private fun mostrarDialogoAgregar() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_reminder, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etSubtitle = dialogView.findViewById<EditText>(R.id.etSubtitle)
        val etTime = dialogView.findViewById<EditText>(R.id.etTime)
        val etBadge = dialogView.findViewById<EditText>(R.id.etBadge)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveReminder)

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

            if (title.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val fechaHoy = sdf.format(Date())

                    database.reminderDao().insertReminder(
                        ReminderEntity(
                            title = title,
                            subtitle = subtitle,
                            time = time,
                            date = fechaHoy,
                            timeRemaining = if (badge.isNotEmpty()) badge else "Hoy",
                            category = "General",
                            status = "Pendiente"
                        )
                    )

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AvisosActivity, "¡Alarma agregada!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        cargarRecordatorios()
                    }
                }
            } else {
                Toast.makeText(this, "Escribe al menos un título", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}