package com.dafi.ruwayspace

import android.os.Bundle
import android.widget.CalendarView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dafi.ruwayspace.data.ExamEntity
import com.dafi.ruwayspace.data.Topic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import com.dafi.ruwayspace.AppDatabase

class CalendarActivity : AppCompatActivity() {
    private lateinit var calendarView: CalendarView

    private lateinit var rvExams: RecyclerView
    private lateinit var rvTopics: RecyclerView

    private lateinit var examAdapter: ExamAdapter
    private lateinit var topicAdapter: TopicAdapter

    private val examList = mutableListOf<ExamEntity>()
    private val topicList = mutableListOf<Topic>()

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        database = AppDatabase.getDatabase(this)
        calendarView = findViewById(R.id.calendarView)

        rvExams = findViewById(R.id.recyclerViewCalendarExams)
        rvTopics = findViewById(R.id.recyclerViewCalendarTopics)

        rvExams.layoutManager = LinearLayoutManager(this)
        rvTopics.layoutManager = LinearLayoutManager(this)

        examAdapter = ExamAdapter(examList) {}
        topicAdapter = TopicAdapter(topicList) {}

        rvExams.adapter = examAdapter
        rvTopics.adapter = topicAdapter

        findViewById<ImageView>(R.id.btnBackCalendar).setOnClickListener { finish() }

        // 💡 Formato numérico estándar (dd/MM/yyyy) para que coincida con el DatePickerDialog
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Cargar fecha actual por defecto
        filtrarPorFecha(sdf.format(Date()))

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            filtrarPorFecha(sdf.format(calendar.time))
        }
    }

    private fun filtrarPorFecha(fechaStr: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val todosExamenes = database.examDao().getAllExams()
            val todosTemas = database.topicDao().getAllTopics()

            val fechaBuscada = fechaStr.trim().lowercase()

            val examenesFiltrados = todosExamenes.filter {
                it.date.trim().lowercase() == fechaBuscada
            }

            val temasFiltrados = todosTemas.filter {
                it.date.trim().lowercase() == fechaBuscada
            }

            withContext(Dispatchers.Main) {
                examList.clear()
                examList.addAll(examenesFiltrados)
                examAdapter.notifyDataSetChanged()

                topicList.clear()
                topicList.addAll(temasFiltrados)
                topicAdapter.notifyDataSetChanged()
            }
        }
    }
}