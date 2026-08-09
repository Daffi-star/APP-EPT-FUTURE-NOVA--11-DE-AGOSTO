package com.dafi.ruwayspace

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.dafi.ruwayspace.data.ExamEntity
import com.dafi.ruwayspace.data.Topic
import com.dafi.ruwayspace.model.SubTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.jvm.java
import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.*


class ExamsActivity : AppCompatActivity() {

    private val topicList = mutableListOf<Topic>()
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val topicListOriginal = mutableListOf<Topic>()
    private lateinit var topicAdapter: TopicAdapter

    private val examList = mutableListOf<ExamEntity>()
    private lateinit var examAdapter: ExamAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exams)

        setupTabs()
        setupRecyclerView()
        setupTopicsRecyclerView()

        cargarTemasDesdeBaseDeDatos()
        cargarExamenesDesdeBaseDeDatos()

        // Botón para ver todos los exámenes
        findViewById<View>(R.id.btnViewAllExams).setOnClickListener {
            startActivity(Intent(this, AllExamsActivity::class.java))
        }

// Botón para ver el calendario
        findViewById<View>(R.id.btnCalendar).setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }

// Botón para ver todos los temas
        findViewById<View>(R.id.btnViewAllTopics).setOnClickListener {
            startActivity(Intent(this, AllTopicsActivity::class.java))
        }

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddTopic)
        fabAdd.setOnClickListener {
            val opciones = arrayOf("📅 Nuevo Examen", "📚 Nuevo Tema / Tarea")
            AlertDialog.Builder(this)
                .setTitle("¿Qué deseas agregar?")
                .setItems(opciones) { _, which ->
                    when (which) {
                        0 -> mostrarDialogoExam(null)
                        1 -> mostrarDialogoTema(null)
                    }
                }
                .show()
        }
    }

    private fun cargarTemasDesdeBaseDeDatos() {
        lifecycleScope.launch(Dispatchers.IO) {
            val listaDb = database.topicDao().getAllTopics()
            withContext(Dispatchers.Main) {
                topicListOriginal.clear()
                topicListOriginal.addAll(listaDb)
                val tabLayout = findViewById<TabLayout>(R.id.tabLayoutTopics)
                val tabSeleccionada = tabLayout.getTabAt(tabLayout.selectedTabPosition)?.text.toString()
                filtrarTemas(if (tabSeleccionada.isBlank()) "Todos" else tabSeleccionada)
            }
        }
    }

    private fun cargarExamenesDesdeBaseDeDatos() {
        lifecycleScope.launch(Dispatchers.IO) {
            val listaDb = database.examDao().getAllExams()
            withContext(Dispatchers.Main) {
                examList.clear()
                examList.addAll(listaDb)
                examAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun setupTabs() {
        val tabs = findViewById<TabLayout>(R.id.tabLayoutTopics)
        if (tabs.tabCount == 0) {
            tabs.addTab(tabs.newTab().setText("Todos"))
            tabs.addTab(tabs.newTab().setText("Pendiente"))
            tabs.addTab(tabs.newTab().setText("En progreso"))
            tabs.addTab(tabs.newTab().setText("Completado"))
        }
        tabs.clearOnTabSelectedListeners()
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> filtrarTemas("Todos")
                    1 -> filtrarTemas("Pendiente")
                    2 -> filtrarTemas("En progreso")
                    3 -> filtrarTemas("Completado")
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun filtrarTemas(estado: String) {
        topicList.clear()
        if (estado == "Todos") {
            topicList.addAll(topicListOriginal)
        } else {
            topicList.addAll(topicListOriginal.filter { it.status.equals(estado, ignoreCase = true) })
        }
        topicAdapter.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        val rvExams = findViewById<RecyclerView>(R.id.rvExams)
        rvExams.layoutManager = LinearLayoutManager(this)
        examAdapter = ExamAdapter(examList) { examSeleccionado ->
            mostrarDialogoExam(examSeleccionado)
        }
        rvExams.adapter = examAdapter
    }

    private fun setupTopicsRecyclerView() {
        val rvTopics = findViewById<RecyclerView>(R.id.rvTopics)
        rvTopics.layoutManager = LinearLayoutManager(this)
        topicAdapter = TopicAdapter(topicList) { topicSeleccionado ->
            mostrarDialogoTema(topicSeleccionado)
        }
        rvTopics.adapter = topicAdapter
    }

    private fun mostrarDialogoExam(examToEdit: ExamEntity?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_exam, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etExamTitle)
        val etSubject = dialogView.findViewById<EditText>(R.id.etExamSubject)
        val etClassroom = dialogView.findViewById<EditText>(R.id.etExamClassroom)
        val etDate = dialogView.findViewById<EditText>(R.id.etExamDate)
        val etDays = dialogView.findViewById<EditText>(R.id.etExamDaysRemaining)
        val etEmoji = dialogView.findViewById<EditText>(R.id.etExamEmoji)
        val spinnerColor = dialogView.findViewById<Spinner>(R.id.spinnerExamColor)

        val colorNames = arrayOf("Azul Suave", "Verde Suave", "Rosa Suave", "Naranja Suave")
        val colorHexes = arrayOf("#E3F2FD", "#E8F5E9", "#FFEBEE", "#FFF3E0")
        spinnerColor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, colorNames)

        if (examToEdit != null) {
            etTitle.setText(examToEdit.title)
            etSubject.setText(examToEdit.subject)
            etClassroom.setText(examToEdit.classroom)
            etDate.setText(examToEdit.date)
            etDays.setText(examToEdit.daysRemaining)
            etEmoji.setText(examToEdit.iconEmoji)
            val colorIndex = colorHexes.indexOf(examToEdit.cardColor).coerceAtLeast(0)
            spinnerColor.setSelection(colorIndex)
        } else {
            etEmoji.setText("📐")
        }

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle(if (examToEdit == null) "Nuevo Examen" else "Editar Examen")
            .setPositiveButton("Guardar") { _, _ ->
                val titulo = etTitle.text.toString()
                if (titulo.isNotBlank()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (examToEdit == null) {
                            database.examDao().insertExam(ExamEntity(
                                title = titulo, subject = etSubject.text.toString(),
                                classroom = etClassroom.text.toString(), date = etDate.text.toString(),
                                daysRemaining = etDays.text.toString(), iconEmoji = if (etEmoji.text.isNotBlank()) etEmoji.text.toString() else "📐",
                                cardColor = colorHexes[spinnerColor.selectedItemPosition]
                            ))
                        } else {
                            examToEdit.apply {
                                title = titulo; subject = etSubject.text.toString(); classroom = etClassroom.text.toString()
                                date = etDate.text.toString(); daysRemaining = etDays.text.toString()
                                iconEmoji = etEmoji.text.toString(); cardColor = colorHexes[spinnerColor.selectedItemPosition]
                            }
                            database.examDao().updateExam(examToEdit)
                        }
                        val listaDb = database.examDao().getAllExams()
                        withContext(Dispatchers.Main) {
                            examList.clear(); examList.addAll(listaDb); examAdapter.notifyDataSetChanged()
                            Toast.makeText(this@ExamsActivity, "Examen guardado", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)

        // Botón de eliminar (Solo visible si estamos editando)
        if (examToEdit != null) {
            builder.setNeutralButton("Eliminar") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    database.examDao().deleteExam(examToEdit)
                    val listaDb = database.examDao().getAllExams()
                    withContext(Dispatchers.Main) {
                        examList.clear(); examList.addAll(listaDb); examAdapter.notifyDataSetChanged()
                        Toast.makeText(this@ExamsActivity, "Examen eliminado", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        builder.show()
    }

    private fun mostrarDialogoTema(topicToEdit: Topic?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_topic, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTopicTitle)
        val etCategory = dialogView.findViewById<EditText>(R.id.etTopicCategory)
        val etEmoji = dialogView.findViewById<EditText>(R.id.etTopicEmoji)
        val etDate = dialogView.findViewById<EditText>(R.id.etTopicDate) // 👈 Nuevo campo de fecha
        val spinnerStatus = dialogView.findViewById<Spinner>(R.id.spinnerTopicStatus)

        val etNewSubtask = dialogView.findViewById<EditText>(R.id.etNewSubtask)
        val btnAddSubtask = dialogView.findViewById<Button>(R.id.btnAddSubtask)
        val containerSubtasks = dialogView.findViewById<LinearLayout>(R.id.containerSubtasks)

        val subtasksList = mutableListOf<SubTask>()
        if (topicToEdit != null) {
            subtasksList.addAll(topicToEdit.subtasks)
        }

        // Variable para almacenar la fecha seleccionada
        var fechaSeleccionada = topicToEdit?.date ?: ""

        // Configurar el selector de fecha al hacer clic en el EditText de fecha
        etDate.setText(fechaSeleccionada)
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                val sdf = SimpleDateFormat("d 'de' MMMM", Locale.forLanguageTag("es-ES"))
                fechaSeleccionada = sdf.format(cal.time)
                etDate.setText(fechaSeleccionada)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        val estados = arrayOf("Pendiente", "En progreso", "Completado")
        spinnerStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados)

        if (topicToEdit != null) {
            etTitle.setText(topicToEdit.title)
            etCategory.setText(topicToEdit.category)
            etEmoji.setText(topicToEdit.emoji)
            spinnerStatus.setSelection(estados.indexOf(topicToEdit.status).coerceAtLeast(0))
        } else {
            etEmoji.setText("📚")
            // Fecha por defecto o vacía al crear uno nuevo
            val sdf = SimpleDateFormat("d 'de' MMMM", Locale.forLanguageTag("es-ES"))
            fechaSeleccionada = sdf.format(Date())
            etDate.setText(fechaSeleccionada)
        }

        // Función para renderizar las subtareas con CheckBox dentro del diálogo
        fun actualizarVistaSubtareas() {
            containerSubtasks.removeAllViews()
            subtasksList.forEach { subTask ->
                val checkBox = android.widget.CheckBox(this).apply {
                    text = subTask.title
                    isChecked = subTask.isCompleted
                    textSize = 14f
                    setOnCheckedChangeListener { _, isChecked ->
                        subTask.isCompleted = isChecked
                    }
                }
                containerSubtasks.addView(checkBox)
            }
        }

        actualizarVistaSubtareas()

        // Botón para agregar una nueva subtarea a la lista temporal
        btnAddSubtask.setOnClickListener {
            val textoSub = etNewSubtask.text.toString().trim()
            if (textoSub.isNotBlank()) {
                subtasksList.add(SubTask(textoSub, false))
                etNewSubtask.text.clear()
                actualizarVistaSubtareas()
            }
        }

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle(if (topicToEdit == null) "Nuevo Tema o Tarea" else "Editar Tema")
            .setPositiveButton("Guardar") { _, _ ->
                val titulo = etTitle.text.toString()
                val categoria = etCategory.text.toString()
                val emoji = if (etEmoji.text.isNotBlank()) etEmoji.text.toString() else "📚"
                val estadoSeleccionado = estados[spinnerStatus.selectedItemPosition]

                val finalSubtasks = if (subtasksList.isEmpty()) {
                    mutableListOf(SubTask("Estudiar conceptos base", false))
                } else {
                    subtasksList
                }

                val completedCount = finalSubtasks.count { it.isCompleted }
                val calculatedProgress = if (finalSubtasks.isNotEmpty()) {
                    (completedCount * 100) / finalSubtasks.size
                } else {
                    0
                }

                val estadoFinal = if (calculatedProgress == 100) "Completado" else estadoSeleccionado

                if (titulo.isNotBlank()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (topicToEdit == null) {
                            val nuevoTema = Topic(
                                emoji = emoji,
                                title = titulo,
                                category = categoria,
                                status = estadoFinal,
                                subtasks = finalSubtasks,
                                date = fechaSeleccionada // 👈 Guardamos la fecha aquí
                            )
                            database.topicDao().insertTopic(nuevoTema)
                        } else {
                            topicToEdit.emoji = emoji
                            topicToEdit.title = titulo
                            topicToEdit.category = categoria
                            topicToEdit.status = estadoFinal
                            topicToEdit.subtasks = finalSubtasks
                            topicToEdit.date = fechaSeleccionada // 👈 Actualizamos la fecha aquí
                            database.topicDao().updateTopic(topicToEdit)
                        }

                        val listaDb = database.topicDao().getAllTopics()
                        withContext(Dispatchers.Main) {
                            topicListOriginal.clear()
                            topicListOriginal.addAll(listaDb)

                            val tabLayout = findViewById<TabLayout>(R.id.tabLayoutTopics)
                            val tabSeleccionada = tabLayout.getTabAt(tabLayout.selectedTabPosition)?.text.toString()
                            filtrarTemas(if (tabSeleccionada.isBlank()) "Todos" else tabSeleccionada)

                            Toast.makeText(this@ExamsActivity, "Tema guardado con éxito", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "El título es obligatorio", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)

        if (topicToEdit != null) {
            builder.setNeutralButton("Eliminar") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    database.topicDao().deleteTopic(topicToEdit)
                    val listaDb = database.topicDao().getAllTopics()
                    withContext(Dispatchers.Main) {
                        topicListOriginal.clear()
                        topicListOriginal.addAll(listaDb)
                        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutTopics)
                        val tabSeleccionada = tabLayout.getTabAt(tabLayout.selectedTabPosition)?.text.toString()
                        filtrarTemas(if (tabSeleccionada.isBlank()) "Todos" else tabSeleccionada)
                        Toast.makeText(this@ExamsActivity, "Tema eliminado", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        builder.show()
    }
}