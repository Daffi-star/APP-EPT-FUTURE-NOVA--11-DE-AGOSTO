package com.dafi.ruwayspace

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dafi.ruwayspace.data.GroupItem
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyGroupsActivity : AppCompatActivity() {

    private lateinit var recyclerGroups: RecyclerView
    private lateinit var fabAddGroup: ExtendedFloatingActionButton
    private val groupList = mutableListOf<GroupItem>()
    private lateinit var groupsAdapter: GroupsAdapter

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_groups)

        // Reemplaza R.id.btnBack por el ID real que tenga tu flecha en el XML
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Cierra la actividad actual y regresa a la anterior
        }

        recyclerGroups = findViewById(R.id.recyclerGroups)
        fabAddGroup = findViewById(R.id.fabAddGroup)

        recyclerGroups.layoutManager = LinearLayoutManager(this)
        groupsAdapter = GroupsAdapter(groupList) { group ->
            // Al hacer clic en un grupo, se abre el chat de esa sala
            val intent = Intent(this, GroupChatActivity::class.java).apply {
                putExtra("ROOM_CODE", group.roomCode)
            }
            startActivity(intent)
        }
        recyclerGroups.adapter = groupsAdapter

        fabAddGroup.setOnClickListener {
            mostrarDialogoCodigoSala()
        }

        cargarMisGrupos()
    }

    private fun cargarMisGrupos() {
        if (currentUserId == null) {
            Log.e("FirebaseError", "currentUserId es NULL. El usuario no ha iniciado sesión.")
            Toast.makeText(this, "Error: No hay sesión activa", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(currentUserId).collection("my_rooms")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FirebaseError", "Error al escuchar mis grupos: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    groupList.clear()
                    for (doc in snapshot.documents) {
                        // Extraemos los campos manualmente para evitar fallos de serialización
                        val code = doc.getString("roomCode") ?: ""
                        val name = doc.getString("groupName") ?: "Sin nombre"

                        if (code.isNotEmpty()) {
                            groupList.add(GroupItem(code, name))
                        }
                    }
                    groupsAdapter.notifyDataSetChanged()
                    Log.d("DEBUG_APP", "Grupos cargados en la lista: ${groupList.size}")
                }
            }
    }

    private fun mostrarDialogoCodigoSala() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_group_manager, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etJoinCode = dialogView.findViewById<EditText>(R.id.etJoinCode)
        val btnJoinRoom = dialogView.findViewById<Button>(R.id.btnJoinRoom)
        val etNewGroupName = dialogView.findViewById<EditText>(R.id.etNewGroupName)
        val etNewGroupCode = dialogView.findViewById<EditText>(R.id.etNewGroupCode)
        val btnCreateRoom = dialogView.findViewById<Button>(R.id.btnCreateRoom)

        // 1. Lógica para unirse a un grupo existente
        btnJoinRoom.setOnClickListener {
            val code = etJoinCode.text.toString().trim().uppercase()
            if (code.isNotEmpty()) {
                db.collection("rooms").document(code).get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val groupName = document.getString("groupName") ?: "Grupo de Estudio"

                        // Guardar en el historial del usuario actual
                        if (currentUserId != null) {
                            val userRoomData = hashMapOf("roomCode" to code, "groupName" to groupName)
                            db.collection("users").document(currentUserId).collection("my_rooms").document(code).set(userRoomData)
                        }

                        dialog.dismiss()
                        val intent = Intent(this, GroupChatActivity::class.java).apply {
                            putExtra("ROOM_CODE", code)
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "El código de sala no existe", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Error al buscar la sala", Toast.LENGTH_SHORT).show()
                }
            } else {
                etJoinCode.error = "Ingresa un código"
            }
        }

        // 2. Lógica para crear un grupo nuevo
        btnCreateRoom.setOnClickListener {
            Log.d("DEBUG_APP", "¡El botón de crear fue presionado!")
            val name = etNewGroupName.text.toString().trim()
            val code = etNewGroupCode.text.toString().trim().uppercase()

            if (name.isNotEmpty() && code.isNotEmpty()) {
                val roomData = hashMapOf(
                    "groupName" to name,
                    "roomCode" to code,
                    "createdAt" to System.currentTimeMillis()
                )

                // Crear la sala en la colección global
                db.collection("rooms").document(code).set(roomData)
                    .addOnSuccessListener {
                        // Guardar en el historial del usuario actual
                        if (currentUserId != null) {
                            val userRoomData = hashMapOf("roomCode" to code, "groupName" to name)
                            db.collection("users").document(currentUserId).collection("my_rooms").document(code).set(userRoomData)
                        }

                        dialog.dismiss()
                        Toast.makeText(this, "¡Grupo creado con éxito!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, GroupChatActivity::class.java).apply {
                            putExtra("ROOM_CODE", code)
                        }
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al crear el grupo", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}