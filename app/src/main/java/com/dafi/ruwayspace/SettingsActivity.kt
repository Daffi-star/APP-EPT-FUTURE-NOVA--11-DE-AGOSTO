package com.dafi.ruwayspace

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Botón de regresar
        findViewById<ImageView>(R.id.btnBackSettings).setOnClickListener {
            finish()
        }

        // Tarjeta: Limpiar completados
        findViewById<MaterialCardView>(R.id.cardCleanCompleted).setOnClickListener {
            confirmarLimpieza("¿Borrar todos los recordatorios completados?", "completed")
        }

        // Tarjeta: Borrar todo
        findViewById<MaterialCardView>(R.id.cardDeleteAll).setOnClickListener {
            confirmarLimpieza("¿Estás seguro de vaciar toda la base de datos?", "all")
        }

        // Tarjeta: Acerca de
        findViewById<MaterialCardView>(R.id.cardAbout).setOnClickListener {
            Toast.makeText(this, "RuwaySpace v1.0 • Diseñado con estilo ✨", Toast.LENGTH_LONG).show()
        }
    }

    private fun confirmarLimpieza(mensaje: String, tipo: String) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar acción")
            .setMessage(mensaje)
            .setPositiveButton("Sí, borrar") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    if (tipo == "completed") {
                        database.reminderDao().deleteCompleted()
                    } else {
                        database.reminderDao().deleteAll()
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SettingsActivity, "¡Limpieza completada!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}