package com.dafi.ruwayspace

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class CulturalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cultural)

        // Botón de retroceso
        findViewById<ImageView>(R.id.btnBackCulture).setOnClickListener {
            finish()
        }

        // Clics de ejemplo en las tarjetas interactivas
        findViewById<MaterialCardView>(R.id.cardSierra).setOnClickListener {
            Toast.makeText(this, "Abriendo información de la Sierra Peruana...", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialCardView>(R.id.cardSelva).setOnClickListener {
            Toast.makeText(this, "Abriendo información de la Selva Peruana...", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialCardView>(R.id.cardJapan).setOnClickListener {
            Toast.makeText(this, "Abriendo información de Japón...", Toast.LENGTH_SHORT).show()
        }

        // Botón de la campana abre la actividad completa de Efemérides
        findViewById<ImageView>(R.id.btnBell).setOnClickListener {
            val intent = Intent(this, EfemeridesActivity::class.java)
            startActivity(intent)
        }
    }
}