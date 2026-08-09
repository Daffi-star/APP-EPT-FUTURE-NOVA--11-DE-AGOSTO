package com.dafi.ruwayspace

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dafi.ruwayspace.data.ExamEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllExamsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExamAdapter
    private val examList = mutableListOf<ExamEntity>()
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_exams)

        database = AppDatabase.getDatabase(this)
        recyclerView = findViewById(R.id.recyclerViewAllExams)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ExamAdapter(examList) {}
        recyclerView.adapter = adapter

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        cargarExamenes()
    }

    private fun cargarExamenes() {
        lifecycleScope.launch(Dispatchers.IO) {
            val lista = database.examDao().getAllExams()
            withContext(Dispatchers.Main) {
                examList.clear()
                examList.addAll(lista)
                adapter.notifyDataSetChanged()
            }
        }
    }
}