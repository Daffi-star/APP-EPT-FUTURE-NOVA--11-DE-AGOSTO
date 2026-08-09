package com.dafi.ruwayspace

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }

    data class EventoAgenda(val hora: String, val titulo: String, val aula: String, val colorPunto: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvSaludo = findViewById<TextView>(R.id.tvUserName)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val nombreUsuario = currentUser?.displayName ?: "Estudiante"
        tvSaludo.text = "¡Hola, $nombreUsuario! 👋"

        configurarFechaActual()

        // 💡 Abrir la pantalla de Avisos al presionar el botón de la barra inferior
        findViewById<View>(R.id.nav_notifications)?.setOnClickListener {
            startActivity(Intent(this, AvisosActivity::class.java))
        }

        // Botón Ver Agenda Completa
        findViewById<View>(R.id.btnVerAgenda).setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }

        // Botón Mis Cursos
        findViewById<LinearLayout>(R.id.btnMisCursos).setOnClickListener {
            startActivity(Intent(this, CoursesActivity::class.java))
        }

        // Botón Exámenes
        findViewById<View>(R.id.btnExams).setOnClickListener {
            startActivity(Intent(this, ExamsActivity::class.java))
        }

        // Botón para ir al Tutor IA
        findViewById<LinearLayout>(R.id.btnTutorIA).setOnClickListener {
            startActivity(Intent(this, TutorActivity::class.java))
        }
    }

    private fun configurarFechaActual() {
        val tvCardDate = findViewById<TextView>(R.id.tvDate)
        if (tvCardDate != null) {
            val sdf = SimpleDateFormat("EEEE, d 'de' MMMM", Locale.forLanguageTag("es-ES"))
            val fechaFormateada = sdf.format(Date())
            tvCardDate.text = fechaFormateada.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarAgendaDeHoy()
        cargarDatosTarjetasResumen()
    }

    private fun cargarDatosTarjetasResumen() {
        lifecycleScope.launch(Dispatchers.IO) {
            val exams = database.examDao().getAllExams()
            val topics = database.topicDao().getAllTopics()

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaHoy = sdf.format(Date())

            // 1. Exámenes
            val totalExamenes = exams.size
            val proximoExamen = exams.firstOrNull()
            val textoDiasExamen = proximoExamen?.daysRemaining?.ifBlank { "Sin fecha" } ?: "Sin exámenes"

            // 2. Tareas pendientes
            val tareasPendientes = topics.count { it.status.equals("Pendiente", ignoreCase = true) }

            // 3. Recordatorios de hoy (Tareas + Exámenes cuya fecha coincida con hoy)
            val recordatoriosHoy = topics.count { it.date.trim().equals(fechaHoy, ignoreCase = true) } +
                    exams.count { it.date.trim().equals(fechaHoy, ignoreCase = true) }

            withContext(Dispatchers.Main) {
                // Tarjeta Exámenes
                findViewById<TextView>(R.id.tvExamCount)?.text = totalExamenes.toString()
                findViewById<TextView>(R.id.tvExamSubtitle)?.text = if (totalExamenes > 0) textoDiasExamen else "Al día"

                // Tarjeta Tareas pendientes
                findViewById<TextView>(R.id.tvTaskCount)?.text = tareasPendientes.toString()
                findViewById<TextView>(R.id.tvTaskSubtitle)?.text = "Por hacer"

                // Tarjeta Recordatorios
                findViewById<TextView>(R.id.tvReminderCount)?.text = recordatoriosHoy.toString()
                findViewById<TextView>(R.id.tvReminderSubtitle)?.text = "Hoy"
            }
        }
    }

    private fun cargarAgendaDeHoy() {
        lifecycleScope.launch(Dispatchers.IO) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaHoy = sdf.format(Date())

            val todosLosTemas = database.topicDao().getAllTopics()
            val temasHoy = todosLosTemas.filter {
                it.date.trim().equals(fechaHoy, ignoreCase = true) && it.time.isNotBlank()
            }

            val listaAgenda = temasHoy.map {
                EventoAgenda(it.time, it.title, it.classroom, "#BA68C8")
            }

            val agendaLimitada = listaAgenda.sortedBy { it.hora }.take(3)

            withContext(Dispatchers.Main) {
                actualizarVistaTimeline(agendaLimitada)
            }
        }
    }

    private fun actualizarVistaTimeline(eventos: List<EventoAgenda>) {
        val row1 = findViewById<View>(R.id.layout_row_1)
        val row2 = findViewById<View>(R.id.layout_row_2)
        val row3 = findViewById<View>(R.id.layout_row_3)
        val layoutEmptyState = findViewById<View>(R.id.layout_empty_state)

        val time1 = findViewById<TextView>(R.id.tv_time_1)
        val title1 = findViewById<TextView>(R.id.tv_title_1)
        val classroom1 = findViewById<TextView>(R.id.tv_classroom_1)

        val time2 = findViewById<TextView>(R.id.tv_time_2)
        val title2 = findViewById<TextView>(R.id.tv_title_2)
        val classroom2 = findViewById<TextView>(R.id.tv_classroom_2)

        val time3 = findViewById<TextView>(R.id.tv_time_3)
        val title3 = findViewById<TextView>(R.id.tv_title_3)
        val classroom3 = findViewById<TextView>(R.id.tv_classroom_3)

        if (eventos.isEmpty()) {
            row1?.visibility = View.GONE
            row2?.visibility = View.GONE
            row3?.visibility = View.GONE
            layoutEmptyState?.visibility = View.VISIBLE
        } else {
            layoutEmptyState?.visibility = View.GONE

            if (eventos.isNotEmpty()) {
                row1?.visibility = View.VISIBLE
                time1?.text = eventos[0].hora
                title1?.text = eventos[0].titulo
                classroom1?.text = eventos[0].aula.ifBlank { "Sin aula" }
            }

            if (eventos.size > 1) {
                row2?.visibility = View.VISIBLE
                time2?.text = eventos[1].hora
                title2?.text = eventos[1].titulo
                classroom2?.text = eventos[1].aula.ifBlank { "Sin aula" }
            } else {
                row2?.visibility = View.INVISIBLE
            }

            if (eventos.size > 2) {
                row3?.visibility = View.VISIBLE
                time3?.text = eventos[2].hora
                title3?.text = eventos[2].titulo
                classroom3?.text = eventos[2].aula.ifBlank { "Sin aula" }
            } else {
                row3?.visibility = View.INVISIBLE
            }
        }
    }
}