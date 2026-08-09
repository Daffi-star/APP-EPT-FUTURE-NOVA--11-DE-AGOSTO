package com.dafi.ruwayspace

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dafi.ruwayspace.data.CourseDao
import com.dafi.ruwayspace.data.CourseEntity
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CoursesActivity : AppCompatActivity() {

    private lateinit var courseDao: CourseDao
    private val masterCourseList = mutableListOf<CourseEntity>()
    private val displayedList = mutableListOf<CourseEntity>()
    private lateinit var courseAdapter: CourseAdapter

    // Manejador de lista temporal de archivos para el diálogo actual (máximo 5)
    private val currentFiles = mutableListOf<String>()
    private var filesContainerRef: LinearLayout? = null
    private var btnSelectPdfRef: Button? = null

    private val pdfPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            if (currentFiles.size < 5) {
                currentFiles.add(it.toString())
                actualizarListaArchivosEnDialogo()
                Toast.makeText(this, "Archivo añadido (${currentFiles.size}/5)", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Límite máximo de 5 archivos alcanzado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courses)

        val btnBackCourses = findViewById<ImageView>(R.id.btnBackCourses)
        val rvCourses = findViewById<RecyclerView>(R.id.rvCourses)
        val fabAddCourse = findViewById<FloatingActionButton>(R.id.fabAddCourse)

        val chipAll = findViewById<Chip>(R.id.chipAll)
        val chipNotStarted = findViewById<Chip>(R.id.chipNotStarted)
        val chipInProgress = findViewById<Chip>(R.id.chipInProgress)
        val chipCompleted = findViewById<Chip>(R.id.chipCompleted)

        courseDao = AppDatabase.getDatabase(this).courseDao()

        courseAdapter = CourseAdapter(displayedList) { course ->
            mostrarDialogoNotion(courseToEdit = course)
        }

        rvCourses.adapter = courseAdapter
        rvCourses.layoutManager = LinearLayoutManager(this)

        btnBackCourses.setOnClickListener { finish() }
        fabAddCourse.setOnClickListener { mostrarDialogoNotion(courseToEdit = null) }

        chipAll.setOnClickListener { filtrarCursos("Todos") }
        chipNotStarted.setOnClickListener { filtrarCursos("Sin empezar") }
        chipInProgress.setOnClickListener { filtrarCursos("En progreso") }
        chipCompleted.setOnClickListener { filtrarCursos("Completado") }

        cargarCursosDesdeDb()
    }

    private fun mostrarDialogoNotion(courseToEdit: CourseEntity?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_notion_course, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etCourseTitle)
        val spinnerStatus = dialogView.findViewById<Spinner>(R.id.spinnerStatus)
        val spinnerDifficulty = dialogView.findViewById<Spinner>(R.id.spinnerDifficulty)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory) // 🏷️ Categoría
        val etDueDate = dialogView.findViewById<EditText>(R.id.etDueDate)             // 📅 Fecha Examen
        val etNotes = dialogView.findViewById<EditText>(R.id.etNotes)                 // 📝 Apuntes

        btnSelectPdfRef = dialogView.findViewById(R.id.btnSelectPdf)
        filesContainerRef = dialogView.findViewById(R.id.filesContainer)

        currentFiles.clear()
        if (courseToEdit != null) {
            currentFiles.addAll(courseToEdit.pdfUris)
        }
        actualizarListaArchivosEnDialogo()

        val statuses = arrayOf("Sin empezar", "En progreso", "Completado")
        val difficulties = arrayOf("Fácil", "Medio", "Difícil")
        val categories = arrayOf("Teoría", "Práctica", "Simulacro", "Lectura") // Opciones libres

        spinnerStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statuses)
        spinnerDifficulty.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, difficulties)
        spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        if (courseToEdit != null) {
            etTitle.setText(courseToEdit.title)
            spinnerStatus.setSelection(statuses.indexOf(courseToEdit.status).coerceAtLeast(0))
            spinnerDifficulty.setSelection(difficulties.indexOf(courseToEdit.difficulty).coerceAtLeast(0))
            spinnerCategory.setSelection(categories.indexOf(courseToEdit.categoryTag).coerceAtLeast(0))
            etDueDate.setText(courseToEdit.dueDate)
            etNotes.setText(courseToEdit.notes)
        }

        btnSelectPdfRef?.setOnClickListener {
            if (currentFiles.size < 5) {
                pdfPickerLauncher.launch("application/pdf")
            } else {
                Toast.makeText(this, "Máximo 5 archivos permitidos", Toast.LENGTH_SHORT).show()
            }
        }

        val tituloVentana = if (courseToEdit == null) "📄 Nueva Página de Curso" else "✏️ Editar Página de Curso"

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle(tituloVentana)
            .setPositiveButton("Guardar") { _, _ ->
                val titulo = etTitle.text.toString()
                val estadoSeleccionado = spinnerStatus.selectedItem.toString()
                val dificultadSeleccionada = spinnerDifficulty.selectedItem.toString()
                val categoriaSeleccionada = spinnerCategory.selectedItem.toString()
                val fechaExamen = etDueDate.text.toString()
                val apuntesNotas = etNotes.text.toString()

                if (titulo.isNotBlank()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val nuevoProgreso = when(estadoSeleccionado) {
                            "Completado" -> 100
                            "En progreso" -> 50
                            else -> 0
                        }

                        if (courseToEdit == null) {
                            courseDao.insertCourse(
                                CourseEntity(
                                    title = titulo,
                                    description = "[$categoriaSeleccionada] • $dificultadSeleccionada",
                                    progress = nuevoProgreso,
                                    status = estadoSeleccionado,
                                    difficulty = dificultadSeleccionada,
                                    pdfUris = currentFiles.toList(),
                                    notes = apuntesNotas,
                                    dueDate = fechaExamen,
                                    categoryTag = categoriaSeleccionada
                                )
                            )
                        } else {
                            val cursoActualizado = courseToEdit.copy(
                                title = titulo,
                                description = "[$categoriaSeleccionada] • $dificultadSeleccionada",
                                progress = nuevoProgreso,
                                status = estadoSeleccionado,
                                difficulty = dificultadSeleccionada,
                                pdfUris = currentFiles.toList(),
                                notes = apuntesNotas,
                                dueDate = fechaExamen,
                                categoryTag = categoriaSeleccionada
                            )
                            courseDao.updateCourse(cursoActualizado)
                        }
                        cargarCursosDesdeDb()
                    }
                } else {
                    Toast.makeText(this, "El título no puede estar vacío", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Dibuja dinámicamente los archivos en el diálogo haciéndolos clicables y con opción de borrar
    private fun actualizarListaArchivosEnDialogo() {
        filesContainerRef?.removeAllViews()

        currentFiles.forEachIndexed { index, uriString ->
            val fileLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 8, 0, 8)
            }

            val tvFile = TextView(this).apply {
                text = "📎 Archivo ${index + 1}: ${Uri.parse(uriString).lastPathSegment ?: "PDF"}"
                setTextColor(Color.parseColor("#2196F3"))
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener { abrirPdf(uriString) } // ¡Clickable para abrirse!
            }

            val btnDelete = TextView(this).apply {
                text = "❌"
                setPadding(16, 0, 0, 0)
                setOnClickListener {
                    currentFiles.removeAt(index)
                    actualizarListaArchivosEnDialogo()
                }
            }

            fileLayout.addView(tvFile)
            fileLayout.fileLayoutAddIfPossible(btnDelete) // O addView genérico
            filesContainerRef?.addView(fileLayout)
        }

        // Deshabilitar botón si llega a 5
        btnSelectPdfRef?.isEnabled = currentFiles.size < 5
        btnSelectPdfRef?.alpha = if (currentFiles.size < 5) 1.0f else 0.5f
    }

    private fun LinearLayout.fileLayoutAddIfPossible(view: android.view.View) {
        this.addView(view)
    }

    private fun cargarCursosDesdeDb() {
        lifecycleScope.launch(Dispatchers.IO) {
            val listaDb = courseDao.getAllCourses()
            withContext(Dispatchers.Main) {
                masterCourseList.clear()
                masterCourseList.addAll(listaDb)
                filtrarCursos("Todos")
            }
        }
    }

    private fun filtrarCursos(estadoFiltro: String) {
        displayedList.clear()
        if (estadoFiltro == "Todos") {
            displayedList.addAll(masterCourseList)
        } else {
            displayedList.addAll(masterCourseList.filter { it.status == estadoFiltro })
        }
        courseAdapter.notifyDataSetChanged()
    }

    fun abrirPdf(uriString: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(uriString), "application/pdf")
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No tienes una aplicación para abrir PDFs", Toast.LENGTH_SHORT).show()
        }
    }
}