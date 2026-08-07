package com.dafi.ruwayspace

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnQuickStart = findViewById<Button>(R.id.btnQuickStart)
        val btnLoginGoogle = findViewById<Button>(R.id.btnLoginGoogle)

        // Por ahora, ambos botones te llevan directo al Dashboard
        val irAlDashboard = {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Cierra el login para no volver atrás al presionar retroceder
        }

        btnQuickStart.setOnClickListener { irAlDashboard() }
        btnLoginGoogle.setOnClickListener { irAlDashboard() }
    }
}