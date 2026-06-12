package com.example.dog_rider_login

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dog_rider_login.adapters.ChatAdapter
import com.example.dog_rider_login.network.RetrofitClient
import com.example.dog_rider_login.network.models.AuthResponse
import com.example.dog_rider_login.network.models.ChatMessage
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
    private val refreshRunnable = object : Runnable {
        override fun run() {
            fetchMessages()
            handler.postDelayed(this, 3000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detalle)

        sessionManager = SessionManager(this)
        emisorEmail = sessionManager.getUserEmail() ?: ""
        idCita = intent.getIntExtra("id_cita", 0)
        receptorEmail = intent.getStringExtra("receptor_email") ?: ""
        val nombreChat = intent.getStringExtra("nombre") ?: "Chat"

        findViewById<TextView>(R.id.tvChatTitle).text = nombreChat
        findViewById<ImageButton>(R.id.btnBackChat).setOnClickListener { finish() }

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
            R.id.emoji4 to "❤️", R.id.emoji5 to "🐕",
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
                    val messages = response.body() ?: emptyList()
                    
                    // Solo actualizamos si el tamaño de la lista cambió
                    if (messages.size != adapter.itemCount) {
                        adapter.updateMessages(messages)
                        findViewById<RecyclerView>(R.id.rvChatMessages).scrollToPosition(messages.size - 1)
                        marcarComoLeidos()
                    }
                }
            }
            override fun onFailure(call: Call<List<ChatMessage>>, t: Throwable) {}
        })
    }

    private fun marcarComoLeidos() {
        val request = mapOf(
            "id_cita" to idCita.toString(),
            "receptor" to emisorEmail // YO soy el receptor de los mensajes que me enviaron
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
