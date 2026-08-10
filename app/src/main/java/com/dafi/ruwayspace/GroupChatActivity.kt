package com.dafi.ruwayspace

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dafi.ruwayspace.data.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

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
}