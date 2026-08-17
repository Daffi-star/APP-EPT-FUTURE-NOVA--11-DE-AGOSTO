package com.dafi.ruwayspace

import android.content.Intent
import android.graphics.BitmapFactory
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
import android.widget.EditText
import kotlin.jvm.java
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Base64

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

        findViewById<View>(R.id.nav_cultura)?.setOnClickListener {
            startActivity(Intent(this, CulturalActivity::class.java))
        }

        findViewById<View>(R.id.btnIrAlGrupo)?.setOnClickListener {
            val intent = Intent(this, MyGroupsActivity::class.java)
            startActivity(intent)
        }

        // Dentro de tu onCreate() en MainActivity.kt
        val ivProfileSettings = findViewById<ImageView>(R.id.ivProfileSettings)

        ivProfileSettings.setOnClickListener {
            val intent = Intent(this, SettingsProfileActivity::class.java)
            startActivity(intent)
        }

        // 💡 Abrir la pantalla de Avisos al presionar el botón de la barra inferior
        findViewById<View>(R.id.nav_avisos)?.setOnClickListener {
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

        cargarFotoEnDashboard()
    }

    private fun cargarFotoEnDashboard() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        val ivProfile = findViewById<ImageView>(R.id.ivProfileSettings)

        db.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
            val base64String = doc.getString("photoBase64")
            if (!base64String.isNullOrEmpty()) {
                val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                ivProfile.setImageBitmap(bitmap)
            }
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
        actualizarContadorRecordatorios()
        cargarPanelGrupal()
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

            withContext(Dispatchers.Main) {
                // Tarjeta Exámenes
                findViewById<TextView>(R.id.tvExamCount)?.text = totalExamenes.toString()
                findViewById<TextView>(R.id.tvExamSubtitle)?.text = if (totalExamenes > 0) textoDiasExamen else "Al día"

                // Tarjeta Tareas pendientes
                findViewById<TextView>(R.id.tvTaskCount)?.text = tareasPendientes.toString()
                findViewById<TextView>(R.id.tvTaskSubtitle)?.text = "Por hacer"
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

    private fun actualizarContadorRecordatorios() {
        // Formato exacto "dd/MM/yyyy" tal como lo manejas en ReminderEntity
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaHoy = formatoFecha.format(Date())

        lifecycleScope.launch(Dispatchers.IO) {
            val totalHoy = AppDatabase.getDatabase(this@MainActivity).reminderDao().getRemindersCountForToday(fechaHoy)

            withContext(Dispatchers.Main) {
                findViewById<TextView>(R.id.tvReminderCount)?.text = totalHoy.toString()
            }
        }
    }

    private fun mostrarDialogoCodigoSala() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_group_manager, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Fondo transparente para que se noten las esquinas redondeadas de tu diseño si las personalizas
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etJoinCode = dialogView.findViewById<EditText>(R.id.etJoinCode)
        val btnJoinRoom = dialogView.findViewById<Button>(R.id.btnJoinRoom)

        val etNewGroupName = dialogView.findViewById<EditText>(R.id.etNewGroupName)
        val etNewGroupCode = dialogView.findViewById<EditText>(R.id.etNewGroupCode)
        val btnCreateRoom = dialogView.findViewById<Button>(R.id.btnCreateRoom)

        val db = FirebaseFirestore.getInstance()

        // OPCIÓN 1: Unirse a sala existente
        btnJoinRoom.setOnClickListener {
            val code = etJoinCode.text.toString().trim().uppercase()
            if (code.isNotEmpty()) {
                db.collection("rooms").document(code).get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        dialog.dismiss()
                        val intent = Intent(this, GroupChatActivity::class.java).apply {
                            putExtra("ROOM_CODE", code)
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "El código de sala no existe", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                etJoinCode.error = "Ingresa un código"
            }
        }

        // OPCIÓN 2: Crear un grupo nuevo
        btnCreateRoom.setOnClickListener {
            val name = etNewGroupName.text.toString().trim()
            val code = etNewGroupCode.text.toString().trim().uppercase()

            if (name.isNotEmpty() && code.isNotEmpty()) {
                val roomData = hashMapOf(
                    "groupName" to name,
                    "roomCode" to code,
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("rooms").document(code).set(roomData)
                    .addOnSuccessListener {
                        dialog.dismiss()
                        Toast.makeText(this, "¡Grupo creado con éxito!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, GroupChatActivity::class.java).apply {
                            putExtra("ROOM_CODE", code)
                        }
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al crear el grupo", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Completa todos los campos para crear el grupo", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun cargarPanelGrupal() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        // Leemos directamente de los grupos del usuario actual
        db.collection("users").document(currentUserId)
            .collection("my_rooms")
            .limit(1) // Tomamos el primer grupo de la lista
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    val groupName = doc.getString("groupName") ?: "Grupo de Estudio"

                    // Actualizamos el TextView del panel con el nombre real
                    findViewById<TextView>(R.id.tvGroupDescription).text = groupName
                } else {
                    findViewById<TextView>(R.id.tvGroupDescription).text = "Aún no te has unido a ningún grupo"
                }
            }
            .addOnFailureListener {
                findViewById<TextView>(R.id.tvGroupDescription).text = "Panel Grupal"
            }
    }
}