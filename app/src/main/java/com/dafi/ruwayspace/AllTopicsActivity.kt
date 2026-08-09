package com.dafi.ruwayspace

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dafi.ruwayspace.data.Topic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllTopicsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TopicAdapter
    private val topicList = mutableListOf<Topic>()
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_topics)

        database = AppDatabase.getDatabase(this)
        recyclerView = findViewById(R.id.recyclerViewAllTopics)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TopicAdapter(topicList) {}
        recyclerView.adapter = adapter

        findViewById<ImageView>(R.id.btnBackTopics).setOnClickListener { finish() }

        cargarTemas()
    }

    private fun cargarTemas() {
        lifecycleScope.launch(Dispatchers.IO) {
            val lista = database.topicDao().getAllTopics()
            withContext(Dispatchers.Main) {
                topicList.clear()
                topicList.addAll(lista)
                adapter.notifyDataSetChanged()
            }
        }
    }
}