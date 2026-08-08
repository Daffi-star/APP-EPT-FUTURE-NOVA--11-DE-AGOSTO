package com.dafi.ruwayspace

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 👇 Fuerza a que la app siempre use el tema claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Dentro de tu MainActivity.kt
        val btnTutorIA = findViewById<LinearLayout>(R.id.btnTutorIA)
        btnTutorIA.setOnClickListener {
            val intent = Intent(this, TutorActivity::class.java)
            startActivity(intent)
        }
    }
}