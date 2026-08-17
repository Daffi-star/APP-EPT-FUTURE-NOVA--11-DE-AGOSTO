package com.dafi.ruwayspace

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dafi.ruwayspace.data.ExamEntity
import com.dafi.ruwayspace.data.Topic
import com.dafi.ruwayspace.model.SubTask
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

        // Dentro de onCreate en ExamsActivity.kt
        val btnBack = findViewById<View>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Esto cierra la actividad actual y regresa a la anterior
        }

        findViewById<View>(R.id.btnViewAllExams).setOnClickListener {
            startActivity(Intent(this, AllExamsActivity::class.java))
        }

        findViewById<View>(R.id.btnCalendar).setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }

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

    // Función auxiliar para el DatePicker (dd/MM/yyyy)
    private fun abrirSelectorFecha(etDestino: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            etDestino.setText(sdf.format(cal.time))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
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

        // Configuración DatePicker
        etDate.isFocusable = false
        etDate.isClickable = true
        etDate.setOnClickListener { abrirSelectorFecha(etDate) }

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

        if (examToEdit != null) {
            builder.setNeutralButton("Eliminar") { _, _ ->
                // 1. Aquí abrimos la alerta de confirmación
                AlertDialog.Builder(this)
                    .setTitle("Eliminar examen")
                    .setMessage("¿Estás seguro de que deseas eliminar '${examToEdit.title}'? Esta acción no se puede deshacer.")
                    // 2. Si el usuario dice "Sí", recién aquí borramos
                    .setPositiveButton("Sí, eliminar") { _, _ ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            database.examDao().deleteExam(examToEdit)
                            val listaDb = database.examDao().getAllExams()
                            withContext(Dispatchers.Main) {
                                examList.clear(); examList.addAll(listaDb); examAdapter.notifyDataSetChanged()
                                Toast.makeText(this@ExamsActivity, "Examen eliminado", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
        builder.show()
    }

    private fun mostrarDialogoTema(topicToEdit: Topic?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_topic, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTopicTitle)
        val etCategory = dialogView.findViewById<EditText>(R.id.etTopicCategory)
        val etEmoji = dialogView.findViewById<EditText>(R.id.etTopicEmoji)
        val etDate = dialogView.findViewById<EditText>(R.id.etTopicDate)
        val etTime = dialogView.findViewById<EditText>(R.id.etTopicTime)
        val etClassroom = dialogView.findViewById<EditText>(R.id.etTopicClassroom)
        val spinnerStatus = dialogView.findViewById<Spinner>(R.id.spinnerTopicStatus)
        val etNewSubtask = dialogView.findViewById<EditText>(R.id.etNewSubtask)
        val btnAddSubtask = dialogView.findViewById<Button>(R.id.btnAddSubtask)
        val containerSubtasks = dialogView.findViewById<LinearLayout>(R.id.containerSubtasks)

        // Configuración DatePicker
        etDate.isFocusable = false
        etDate.isClickable = true
        etDate.setOnClickListener { abrirSelectorFecha(etDate) }

        val subtasksList = mutableListOf<SubTask>()
        if (topicToEdit != null) subtasksList.addAll(topicToEdit.subtasks)

        etTime.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(this, { _, h, m ->
                etTime.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m))
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        val estados = arrayOf("Pendiente", "En progreso", "Completado")
        spinnerStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados)

        if (topicToEdit != null) {
            etTitle.setText(topicToEdit.title)
            etCategory.setText(topicToEdit.category)
            etEmoji.setText(topicToEdit.emoji)
            etDate.setText(topicToEdit.date)
            etTime.setText(topicToEdit.time)
            etClassroom.setText(topicToEdit.classroom)
            spinnerStatus.setSelection(estados.indexOf(topicToEdit.status).coerceAtLeast(0))
        } else {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            etDate.setText(sdf.format(Date()))
            etEmoji.setText("📚")
        }

        fun actualizarVistaSubtareas() {
            containerSubtasks.removeAllViews()
            subtasksList.forEach { subTask ->
                val checkBox = CheckBox(this).apply {
                    text = subTask.title; isChecked = subTask.isCompleted
                    setOnCheckedChangeListener { _, b -> subTask.isCompleted = b }
                }
                containerSubtasks.addView(checkBox)
            }
        }
        actualizarVistaSubtareas()

        btnAddSubtask.setOnClickListener {
            val t = etNewSubtask.text.toString().trim()
            if (t.isNotBlank()) { subtasksList.add(SubTask(t, false)); etNewSubtask.text.clear(); actualizarVistaSubtareas() }
        }

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle(if (topicToEdit == null) "Nuevo Tema" else "Editar Tema")
            .setPositiveButton("Guardar") { _, _ ->
                val titulo = etTitle.text.toString()
                if (titulo.isNotBlank()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (topicToEdit == null) {
                            database.topicDao().insertTopic(Topic(
                                emoji = etEmoji.text.toString().ifBlank { "📚" },
                                title = titulo, category = etCategory.text.toString(),
                                status = estados[spinnerStatus.selectedItemPosition],
                                subtasks = subtasksList, date = etDate.text.toString(),
                                classroom = etClassroom.text.toString(), time = etTime.text.toString()
                            ))
                        } else {
                            topicToEdit.apply {
                                emoji = etEmoji.text.toString(); title = titulo; category = etCategory.text.toString()
                                status = estados[spinnerStatus.selectedItemPosition]; subtasks = subtasksList
                                date = etDate.text.toString(); time = etTime.text.toString(); classroom = etClassroom.text.toString()
                            }
                            database.topicDao().updateTopic(topicToEdit)
                        }
                        cargarTemasDesdeBaseDeDatos()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ExamsActivity, "Tema guardado", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)

        // 🗑️ CORRECCIÓN AQUÍ: Usamos topicToEdit en lugar de examToEdit
        if (topicToEdit != null) {
            builder.setNeutralButton("Eliminar") { _, _ ->
                // 1. Abrimos la alerta de confirmación
                AlertDialog.Builder(this)
                    .setTitle("Eliminar tema")
                    .setMessage("¿Estás seguro de que deseas eliminar '${topicToEdit.title}'?")
                    // 2. Si el usuario confirma, borramos
                    .setPositiveButton("Sí, eliminar") { _, _ ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            database.topicDao().deleteTopic(topicToEdit)
                            cargarTemasDesdeBaseDeDatos()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@ExamsActivity, "Tema eliminado", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        builder.show()
    }
}