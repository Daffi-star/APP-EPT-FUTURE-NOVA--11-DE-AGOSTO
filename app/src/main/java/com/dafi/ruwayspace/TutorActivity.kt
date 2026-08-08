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
import com.dafi.ruwayspace.data.ChatDao
import com.dafi.ruwayspace.data.ChatMessageEntity
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TutorActivity : AppCompatActivity() {

    private lateinit var chatSession: Chat
    private lateinit var chatDao: ChatDao
    private val messageList = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatMessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutor)

        val inputPregunta = findViewById<EditText>(R.id.inputPregunta)
        val btnEnviar = findViewById<Button>(R.id.btnEnviar)
        val rvChat = findViewById<RecyclerView>(R.id.rvChat)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        // Inicializar Room DAO
        chatDao = AppDatabase.getDatabase(this).chatDao()

        // Configurar adaptador
        adapter = ChatMessageAdapter(messageList)
        rvChat.adapter = adapter
        rvChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }

        // Inicializar Gemini y cargar el historial con la memoria de la IA
        val generativeModel = GenerativeModel(
            modelName = "gemini-flash-latest",
            apiKey = BuildConfig.GEMINI_API_KEY
        )

        cargarHistorialYConfigurarChat(generativeModel, adapter, rvChat)

        // Botón de retroceso
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Botón Enviar
        btnEnviar.setOnClickListener {
            val textoUsuario = inputPregunta.text.toString().trim()

            if (textoUsuario.isNotEmpty()) {
                inputPregunta.text.clear()

                // 1. Mostrar y guardar mensaje del usuario
                agregarYGuardarMensaje(textoUsuario, isUser = true, adapter, rvChat)

                // 2. Mostrar mensaje temporal "Pensando..."
                val posicionPensando = messageList.size
                messageList.add(ChatMessage("Pensando...", false))
                adapter.notifyItemInserted(posicionPensando)
                rvChat.scrollToPosition(posicionPensando)

                // 3. Llamar a la API de la IA
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val response = chatSession.sendMessage(textoUsuario)
                        val respuestaReal = response.text ?: "No se pudo obtener una respuesta."

                        withContext(Dispatchers.Main) {
                            // Reemplazar "Pensando..." por la respuesta real
                            messageList[posicionPensando] = ChatMessage(respuestaReal, false)
                            adapter.notifyItemChanged(posicionPensando)
                            rvChat.scrollToPosition(posicionPensando)

                            // Guardar respuesta del bot en base de datos
                            chatDao.insertMessage(
                                ChatMessageEntity(
                                    text = respuestaReal,
                                    isUser = false
                                )
                            )
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            messageList[posicionPensando] = ChatMessage("Error al conectar con la IA.", false)
                            adapter.notifyItemChanged(posicionPensando)
                        }
                    }
                }
            }
        }
    }

    // Función única que lee Room, reconstruye la memoria de la IA y pinta la pantalla
    private fun cargarHistorialYConfigurarChat(
        generativeModel: GenerativeModel,
        adapter: ChatMessageAdapter,
        rvChat: RecyclerView
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            val historialDb = chatDao.getAllMessages()

            // Convertir historial al formato que entiende Gemini
            val historialParaIa = historialDb.map { entity ->
                val role = if (entity.isUser) "user" else "model"
                content(role) {
                    text(entity.text)
                }
            }

            withContext(Dispatchers.Main) {
                // Inicializar la sesión con la memoria pasada
                chatSession = generativeModel.startChat(history = historialParaIa)

                // Pintar mensajes en la interfaz
                messageList.clear()
                for (item in historialDb) {
                    messageList.add(ChatMessage(item.text, item.isUser))
                }
                adapter.notifyDataSetChanged()

                if (messageList.isNotEmpty()) {
                    rvChat.scrollToPosition(messageList.size - 1)
                }
            }
        }
    }

    private fun agregarYGuardarMensaje(
        texto: String,
        isUser: Boolean,
        adapter: ChatMessageAdapter,
        rvChat: RecyclerView
    ) {
        messageList.add(ChatMessage(texto, isUser))
        adapter.notifyItemInserted(messageList.size - 1)
        rvChat.scrollToPosition(messageList.size - 1)

        lifecycleScope.launch(Dispatchers.IO) {
            chatDao.insertMessage(ChatMessageEntity(text = texto, isUser = isUser))
        }
    }
}