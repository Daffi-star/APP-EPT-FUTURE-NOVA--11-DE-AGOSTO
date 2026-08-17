package com.dafi.ruwayspace

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsProfileActivity : AppCompatActivity() {

    private lateinit var etProfileName: TextInputEditText
    private lateinit var etProfileBio: TextInputEditText
    private lateinit var etProfileEmail: TextInputEditText
    private lateinit var ivProfileImage: ImageView
    private lateinit var tvUserBadge: TextView
    private lateinit var switchFocusMode: SwitchMaterial
    private lateinit var switchNotifications: SwitchMaterial
    private lateinit var tvFocusStatus: TextView // Nuevo texto para el cronómetro

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val CHANNEL_ID = "ruway_space_notifications"

    private var countDownTimer: CountDownTimer? = null
    private var isUserToggling = false // Para evitar bucles al apagar el switch por código

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            ivProfileImage.setImageURI(uri)
            guardarImagenEnFirestore(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_profile)

        etProfileName = findViewById(R.id.etProfileName)
        etProfileBio = findViewById(R.id.etProfileBio)
        etProfileEmail = findViewById(R.id.etProfileEmail)
        ivProfileImage = findViewById(R.id.ivProfileImage)
        tvUserBadge = findViewById(R.id.tvUserBadge)
        switchFocusMode = findViewById(R.id.switchFocusMode)
        switchNotifications = findViewById(R.id.switchNotifications)
        tvFocusStatus = findViewById(R.id.tvFocusStatus)

        crearCanalNotificacion()
        cargarDatosUsuario()
        cargarPreferenciasLocales()
        configurarSwitches()

        findViewById<ImageView>(R.id.btnBackSettings).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvChangePhoto).setOnClickListener { pickImageLauncher.launch("image/*") }
        findViewById<Button>(R.id.btnSaveProfile).setOnClickListener { guardarCambios() }
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut() // 1. Cerramos la sesión en Firebase

            // 2. Preparamos el camino para volver al Login
            // (Asegúrate de que 'LoginActivity' sea el nombre de tu clase de login)
            val intent = Intent(this, LoginActivity::class.java)

            // 3. Estas banderas limpian el historial de pantallas
            // para que no puedan volver atrás después de cerrar sesión
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent) // 4. Vamos a la pantalla de login
            finish() // 5. Cerramos el perfil actual
        }
    }

    private fun cargarPreferenciasLocales() {
        val prefs = getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)

        // Cargar Modo Enfoque
        val isFocus = prefs.getBoolean("focus_mode", false)
        switchFocusMode.isChecked = isFocus
        if (isFocus) {
            tvFocusStatus.text = "Modo Enfoque activo (Pausado por reinicio)"
        }

        // NUEVO: Cargar Notificaciones
        val isNotifications = prefs.getBoolean("notifications_enabled", false)
        switchNotifications.isChecked = isNotifications
    }

    private fun configurarSwitches() {
        switchFocusMode.setOnCheckedChangeListener { _, isChecked ->
            if (isUserToggling) return@setOnCheckedChangeListener

            val prefs = getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("focus_mode", isChecked).apply()

            if (isChecked) {
                // En lugar de arrancar de golpe, mostramos opciones de tiempo
                mostrarSelectorDeTiempo()
            } else {
                detenerModoEnfoque()
            }
        }

        // --- Lógica de Notificaciones ---
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            // Guardamos el estado para que no se apague al salir
            val prefs = getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()

            if (isChecked) {
                Toast.makeText(this, "Abriendo configuración de notificaciones...", Toast.LENGTH_SHORT).show()
                abrirConfiguracionDeNotificaciones()
            } else {
                Toast.makeText(this, "Notificaciones desactivadas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarSelectorDeTiempo() {
        val opciones = arrayOf("15 minutos", "25 minutos (Pomodoro)", "45 minutos", "60 minutos")
        val minutosValues = arrayOf(15, 25, 45, 60)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("⏱️ Elige tu tiempo de estudio")
        builder.setItems(opciones) { _, which ->
            val minutosElegidos = minutosValues[which]
            iniciarContador(minutosElegidos)
        }
        // Si el usuario cancela la ventana flotante, apagamos el switch
        builder.setOnCancelListener {
            isUserToggling = true
            switchFocusMode.setChecked(false)
            isUserToggling = false
            tvFocusStatus.text = ""
        }
        builder.show()
    }

    private fun iniciarContador(minutos: Int) {
        val milisegundos = minutos * 60 * 1000L
        Toast.makeText(this, "¡Modo Enfoque iniciado por $minutos min!", Toast.LENGTH_SHORT).show()

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(milisegundos, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val min = millisUntilFinished / 1000 / 60
                val sec = (millisUntilFinished / 1000) % 60
                tvFocusStatus.text = String.format("Tiempo restante: %02d:%02d", min, sec)
            }

            override fun onFinish() {
                tvFocusStatus.text = "¡Sesión de enfoque terminada! 🎉"
                isUserToggling = true
                switchFocusMode.setChecked(false)
                isUserToggling = false

                val prefs = getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("focus_mode", false).apply()

                Toast.makeText(this@SettingsProfileActivity, "¡Tiempo cumplido, gran trabajo!", Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    private fun detenerModoEnfoque() {
        countDownTimer?.cancel()
        tvFocusStatus.text = ""
        Toast.makeText(this, "Modo Enfoque desactivado", Toast.LENGTH_SHORT).show()
    }

    private fun cargarDatosUsuario() {
        val user = auth.currentUser
        if (user != null) {
            // AQUÍ ESTÁ EL CAMBIO: Llenamos el email directamente desde la sesión
            // que Firebase ya tiene abierta, sin necesidad de consultar la base de datos.
            etProfileEmail.setText(user.email)

            // Luego, cargamos el resto desde Firestore (nombre, bio, foto)
            db.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    etProfileName.setText(doc.getString("name"))
                    etProfileBio.setText(doc.getString("bio"))

                    val base64String = doc.getString("photoBase64")
                    if (!base64String.isNullOrEmpty()) {
                        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        ivProfileImage.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }

    private fun guardarImagenEnFirestore(uri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val inputStream = contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        val base64String = Base64.encodeToString(bytes, Base64.DEFAULT)
        db.collection("users").document(userId).update("photoBase64", base64String)
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "RuwaySpace", NotificationManager.IMPORTANCE_HIGH)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun guardarCambios() {
        val userId = auth.currentUser?.uid ?: return
        val userData = hashMapOf("name" to etProfileName.text.toString(), "bio" to etProfileBio.text.toString())
        db.collection("users").document(userId).set(userData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener { Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show() }
    }

    private fun abrirConfiguracionDeNotificaciones() {
        try {
            val intent = android.content.Intent().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Para Android 8.0 y superior
                    action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName)
                } else {
                    // Para Android más antiguo
                    action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.parse("package:$packageName")
                }
            }
            startActivity(intent)
        } catch (e: Exception) {
            // Plan B por si el teléfono bloquea la acción principal
            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }
}