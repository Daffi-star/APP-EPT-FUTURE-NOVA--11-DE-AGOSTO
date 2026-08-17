package com.dafi.ruwayspace

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dafi.ruwayspace.data.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class GroupChatActivity : AppCompatActivity() {

    private lateinit var roomCode: String
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val messageList = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        // Recibimos el código de sala enviado desde el diálogo
        roomCode = intent.getStringExtra("ROOM_CODE")?.uppercase() ?: "GENERAL"

        val tvTitle = findViewById<TextView>(R.id.tvChatTitle)
        tvTitle.text = "Sala: $roomCode"

        findViewById<ImageView>(R.id.btnBackChat)?.setOnClickListener { finish() }

        // Botón para salir del grupo
        findViewById<ImageView>(R.id.btnExitGroup)?.setOnClickListener {
            mostrarDialogoSalida()
        }

        setupRecyclerView()
        setupListeners()
        cargarMensajesEnTiempoReal()
    }

    private fun setupRecyclerView() {
        val recycler = findViewById<RecyclerView>(R.id.recyclerChat)
        chatAdapter = ChatAdapter(messageList)
        recycler.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recycler.adapter = chatAdapter
    }

    private fun setupListeners() {
        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSend = findViewById<ImageButton>(R.id.btnSend)

        btnSend.setOnClickListener {
            val texto = etMessage.text.toString().trim()
            if (texto.isNotEmpty()) {
                enviarMensaje(texto)
                etMessage.text.clear()
            }
        }
    }

    private fun enviarMensaje(texto: String) {
        val user = auth.currentUser
        val senderId = user?.uid ?: "anónimo"
        val senderName = user?.displayName ?: "Estudiante"

        val mensaje = ChatMessage(
            senderId = senderId,
            senderName = senderName,
            message = texto,
            timestamp = System.currentTimeMillis()
        )

        db.collection("rooms")
            .document(roomCode)
            .collection("messages")
            .add(mensaje)
            .addOnFailureListener {
                Toast.makeText(this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarMensajesEnTiempoReal() {
        db.collection("rooms")
            .document(roomCode)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                if (snapshot != null) {
                    messageList.clear()
                    for (doc in snapshot.documents) {
                        val msg = doc.toObject(ChatMessage::class.java)
                        if (msg != null) {
                            messageList.add(msg)
                        }
                    }
                    chatAdapter.notifyDataSetChanged()
                    if (messageList.isNotEmpty()) {
                        findViewById<RecyclerView>(R.id.recyclerChat).scrollToPosition(messageList.size - 1)
                    }
                }
            }
    }

    // 🚪 Función corregida para eliminar el grupo de tu lista personal
    private fun mostrarDialogoSalida() {
        val userId = auth.currentUser?.uid ?: return

        AlertDialog.Builder(this)
            .setTitle("Salir del grupo")
            .setMessage("¿Estás seguro de que deseas salir de la sala $roomCode? Ya no aparecerá en tus grupos.")
            .setPositiveButton("Sí, salir") { _, _ ->
                // Eliminamos el documento del grupo en la subcolección personal del usuario
                db.collection("users").document(userId)
                    .collection("my_rooms").document(roomCode)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Has salido del grupo", Toast.LENGTH_SHORT).show()
                        finish() // Cierra el chat y regresa a la pantalla anterior
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al salir: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}