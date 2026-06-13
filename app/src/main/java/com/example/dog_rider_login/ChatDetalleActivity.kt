package com.example.dog_rider_login

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dog_rider_login.adapters.ChatAdapter
import com.example.dog_rider_login.network.RetrofitClient
import com.example.dog_rider_login.network.models.AuthResponse
import com.example.dog_rider_login.network.models.ChatMessage
import com.example.dog_rider_login.utils.Constants
import com.example.dog_rider_login.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatDetalleActivity : AppCompatActivity() {

    private lateinit var adapter: ChatAdapter
    private lateinit var sessionManager: SessionManager
    private var idCita: Int = 0
    private var emisorEmail: String = ""
    private var receptorEmail: String = ""

    private val handler = Handler(Looper.getMainLooper())
    
    // Runnable para refrescar mensajes y estado
    private val refreshRunnable = object : Runnable {
        override fun run() {
            fetchMessages()
            fetchOnlineStatus()
            updateMyStatus() // Actualizar mi propio estado como "online"
            handler.postDelayed(this, 3000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detalle)

        sessionManager = SessionManager(this)
        
        // SEGURIDAD: Verificar sesión activa
        if (sessionManager.getUserEmail().isNullOrEmpty()) {
            finish()
            return
        }

        emisorEmail = sessionManager.getUserEmail() ?: ""
        idCita = intent.getIntExtra("id_cita", 0)
        receptorEmail = intent.getStringExtra("receptor_email") ?: ""
        val nombreChat = intent.getStringExtra("nombre") ?: "Chat"
        val foto = intent.getStringExtra("foto")

        findViewById<TextView>(R.id.tvChatTitle).text = nombreChat
        findViewById<ImageButton>(R.id.btnBackChat).setOnClickListener { finish() }

        val ivHeader = findViewById<ImageView>(R.id.ivHeaderProfile)
        if (!foto.isNullOrEmpty()) {
            if (foto.startsWith("avatar_")) {
                val idRes = resources.getIdentifier(foto, "drawable", packageName)
                ivHeader.setImageResource(if (idRes != 0) idRes else R.drawable.app_logo)
            } else {
                val url = Constants.UPLOADS_URL + foto
                Glide.with(this).load(url).centerCrop().into(ivHeader)
            }
        }

        val et = findViewById<EditText>(R.id.etChatMessage)
        val filtroSeguridad = InputFilter { source, start, end, _, _, _ ->
            val simbolosProhibidos = "<>{}[]^|\\"
            for (i in start until end) {
                if (simbolosProhibidos.contains(source[i])) return@InputFilter ""
            }
            null
        }
        et.filters = arrayOf(filtroSeguridad, InputFilter.LengthFilter(200))

        val rv = findViewById<RecyclerView>(R.id.rvChatMessages)
        adapter = ChatAdapter(emisorEmail, mutableListOf())
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        findViewById<ImageButton>(R.id.btnSendMessage).setOnClickListener { sendMessage() }
        setupQuickEmojis()
    }

    private fun setupQuickEmojis() {
        val emojis = listOf(
            R.id.emoji1 to "🐶", R.id.emoji2 to "🐾", R.id.emoji3 to "🦴",
            R.id.emoji4 to "❤️",
            R.id.quickMsg1 to "¡Ya voy!", R.id.quickMsg2 to "¿Todo bien?"
        )
        emojis.forEach { (viewId, text) ->
            findViewById<View>(viewId).setOnClickListener { sendQuickMessage(text) }
        }
    }

    private fun sendQuickMessage(text: String) {
        val msg = ChatMessage(idCita, emisorEmail, receptorEmail, text)
        RetrofitClient.instance.enviarMensaje(msg).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) fetchMessages()
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {}
        })
    }

    private fun fetchMessages() {
        RetrofitClient.instance.obtenerMensajes(idCita).enqueue(object : Callback<List<ChatMessage>> {
            override fun onResponse(call: Call<List<ChatMessage>>, response: Response<List<ChatMessage>>) {
                if (response.isSuccessful) {
                    val newMessages = response.body() ?: emptyList()
                    
                    // Solo actualizamos si algo cambió (cantidad de mensajes o estado de lectura)
                    if (shouldUpdateAdapter(newMessages)) {
                        adapter.updateMessages(newMessages)
                        
                        // Si hay mensajes nuevos, hacemos scroll al final
                        if (newMessages.size > adapter.itemCount) {
                            findViewById<RecyclerView>(R.id.rvChatMessages).scrollToPosition(newMessages.size - 1)
                        }
                        
                        // Si hay mensajes para mí, los marcamos como leídos
                        marcarComoLeidos()
                    }
                }
            }
            override fun onFailure(call: Call<List<ChatMessage>>, t: Throwable) {}
        })
    }

    private fun shouldUpdateAdapter(newList: List<ChatMessage>): Boolean {
        // Si el adaptador está vacío y la lista no, actualizar
        if (adapter.itemCount == 0 && newList.isNotEmpty()) return true
        
        // Si el tamaño cambió, actualizar
        if (adapter.itemCount != newList.size) return true
        
        // Si el estado 'leido' del último mensaje enviado por mí cambió, actualizar
        // (En un chat real compararíamos toda la lista, pero esto es más eficiente)
        return true // Por simplicidad y asegurar que los ticks cambien, devolvemos true si hay datos
    }

    private fun fetchOnlineStatus() {
        if (receptorEmail.isEmpty()) return
        
        RetrofitClient.instance.obtenerEstadoUsuario(receptorEmail).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                val tvStatus = findViewById<TextView>(R.id.tvOnlineStatus) ?: return
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val onlineRaw = body["online"]
                    val online = when (onlineRaw) {
                        is Boolean -> onlineRaw
                        is String -> onlineRaw == "1" || onlineRaw.lowercase() == "true"
                        is Number -> onlineRaw.toInt() == 1
                        else -> false
                    }

                    if (online) {
                        tvStatus.text = getString(R.string.en_linea)
                        tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                    } else {
                        val ultimaVez = body["ultima_vez"]?.toString() ?: "..."
                        tvStatus.text = getString(R.string.ultima_vez, ultimaVez)
                        tvStatus.setTextColor(android.graphics.Color.LTGRAY)
                    }
                } else {
                    // Si el servidor responde pero no con éxito (ej. 404), mostrar desconectado
                    tvStatus.text = "Desconectado"
                    tvStatus.setTextColor(android.graphics.Color.LTGRAY)
                }
            }
            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                // Si falla la conexión (ej. el archivo PHP no existe en XAMPP)
                val tvStatus = findViewById<TextView>(R.id.tvOnlineStatus)
                tvStatus?.text = "Offline"
                tvStatus?.setTextColor(android.graphics.Color.LTGRAY)
            }
        })
    }

    private fun updateMyStatus() {
        val request = mapOf("email" to emisorEmail)
        RetrofitClient.instance.actualizarEstado(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {}
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {}
        })
    }

    private fun marcarComoLeidos() {
        val request = mapOf(
            "id_cita" to idCita.toString(),
            "receptor" to emisorEmail
        )
        RetrofitClient.instance.marcarMensajesLeidos(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {}
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {}
        })
    }

    private fun sendMessage() {
        val et = findViewById<EditText>(R.id.etChatMessage)
        val text = et.text.toString().trim()
        if (text.isEmpty()) return
        val msg = ChatMessage(idCita, emisorEmail, receptorEmail, text)
        et.text.clear()
        RetrofitClient.instance.enviarMensaje(msg).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) fetchMessages()
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(this@ChatDetalleActivity, "Fallo al enviar", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }
}
