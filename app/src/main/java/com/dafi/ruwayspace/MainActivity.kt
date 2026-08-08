package com.dafi.ruwayspace

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView // <-- Importante
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth // <-- Importante


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 👇 Fuerza a que la app siempre use el tema claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referencia a tu TextView de saludo (reemplaza 'tvSaludo' si tu ID es diferente en el XML)
        val tvSaludo = findViewById<TextView>(R.id.tvUserName)

        // Obtener el usuario actual de Firebase Authentication
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Si hay cuenta de Google, toma su nombre; si no (entró por Quick Start), usa "Estudiante"
        val nombreUsuario = currentUser?.displayName ?: "Estudiante"

        // Asignar el texto dinámicamente con la mano saludando
        tvSaludo.text = "¡Hola, $nombreUsuario! 👋"

        // Agrega esto dentro del onCreate de tu MainActivity:
        val btnMisCursos = findViewById<LinearLayout>(R.id.btnMisCursos) // Asegúrate de poner el ID correcto de tu tarjeta de cursos en el activity_main.xml
        btnMisCursos.setOnClickListener {
            val intent = Intent(this, CoursesActivity::class.java)
            startActivity(intent)
        }

        // Botón para ir al Tutor IA
        val btnTutorIA = findViewById<LinearLayout>(R.id.btnTutorIA)
        btnTutorIA.setOnClickListener {
            val intent = Intent(this, TutorActivity::class.java)
            startActivity(intent)
        }
    }
}