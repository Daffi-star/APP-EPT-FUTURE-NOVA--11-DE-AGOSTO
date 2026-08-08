package com.dafi.ruwayspace

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

class TutorActivity : AppCompatActivity() {

    private lateinit var chatSession: Chat

    private fun llamarApiDeIa(pregunta: String, onResult: (String) -> Unit) {
        // Llamada real a la IA utilizando la sesión de chat con memoria
        lifecycleScope.launch {
            try {
                val response = chatSession.sendMessage(pregunta)
                val respuestaReal = response.text ?: "No se pudo obtener una respuesta."
                onResult(respuestaReal)
            } catch (e: Exception) {
                onResult("Error al conectar con la IA: ${e.localizedMessage}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutor)

        val inputPregunta = findViewById<EditText>(R.id.inputPregunta)
        val btnEnviar = findViewById<Button>(R.id.btnEnviar)
        val rvChat = findViewById<RecyclerView>(R.id.rvChat)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        // Configurar la lista y el adaptador
        val messageList = mutableListOf<ChatMessage>()
        val adapter = ChatMessageAdapter(messageList)
        rvChat.adapter = adapter
        rvChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }

        // Inicializar el modelo de Gemini (usando un modelo estable y estándar)
        val generativeModel = GenerativeModel(
            modelName = "gemini-flash-latest",
            apiKey = BuildConfig.GEMINI_API_KEY
        )

        // Inicializar el chat con memoria
        chatSession = generativeModel.startChat()

        // 1. Configurar el botón de retroceso
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 2. Lógica limpia al hacer clic en el botón Enviar
        btnEnviar.setOnClickListener {
            val textoUsuario = inputPregunta.text.toString().trim()

            if (textoUsuario.isNotEmpty()) {
                // PASO 1: Añadir tu mensaje a la lista y notificar al adaptador
                messageList.add(ChatMessage(textoUsuario, true))
                adapter.notifyItemInserted(messageList.size - 1)
                inputPregunta.text.clear()
                rvChat.scrollToPosition(messageList.size - 1)

                // PASO 2: Guardar la posición exacta del mensaje "Pensando..."
                val posicionPensando = messageList.size

                // PASO 3: Añadir el mensaje temporal del bot
                messageList.add(ChatMessage("Pensando...", false))
                adapter.notifyItemInserted(posicionPensando)
                rvChat.scrollToPosition(posicionPensando)

                // PASO 4: Hacer la consulta real a la API de IA
                llamarApiDeIa(textoUsuario) { respuestaReal ->
                    // PASO 5: Reemplazar "Pensando..." por el texto real devuelto por Gemini
                    messageList[posicionPensando] = ChatMessage(respuestaReal, false)
                    adapter.notifyItemChanged(posicionPensando)
                    rvChat.scrollToPosition(posicionPensando)
                }
            }
        }
    }
}