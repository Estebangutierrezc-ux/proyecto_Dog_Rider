package com.example.dog_rider_login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dog_rider_login.adapters.ActiveChatAdapter
import com.example.dog_rider_login.network.RetrofitClient
import com.example.dog_rider_login.network.models.CitaRequest
import com.example.dog_rider_login.utils.NavigationUtils
import com.example.dog_rider_login.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: ActiveChatAdapter
    private var allChats = listOf<CitaRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        sessionManager = SessionManager(this)
        
        // SEGURIDAD: Verificar sesión activa
        if (sessionManager.getUserEmail().isNullOrEmpty()) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        NavigationUtils.configurarNavegacion(this)

        setupRecyclerView()
        checkActiveChats()
    }

    private fun setupRecyclerView() {
        val rv = findViewById<RecyclerView>(R.id.rvActiveChats) ?: return
        adapter = ActiveChatAdapter(emptyList()) { paseo ->
            val intent = Intent(this, ChatDetalleActivity::class.java)
            intent.putExtra("id_cita", paseo.id)
            
            val isWalker = sessionManager.isWalker()
            val receptorEmail = if (isWalker) paseo.usuarioEmail else paseo.paseadorEmail
            val nombreReceptor = if (isWalker) {
                paseo.duenoNombre ?: getString(R.string.default_owner_name)
            } else {
                getString(R.string.format_walker_chat_name, paseo.mascota)
            }

            intent.putExtra("receptor_email", receptorEmail)
            intent.putExtra("nombre", nombreReceptor)
            startActivity(intent)
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }

    private fun checkActiveChats() {
        val email = sessionManager.getUserEmail() ?: ""
        val isWalker = sessionManager.isWalker()

        if (isWalker) {
            RetrofitClient.instance.obtenerPaseosActivos(email).enqueue(object : Callback<List<CitaRequest>> {
                override fun onResponse(call: Call<List<CitaRequest>>, response: Response<List<CitaRequest>>) {
                    if (response.isSuccessful && response.body() != null) {
                        allChats = response.body()!!
                        if (allChats.isNotEmpty()) {
                            showChatUI()
                        } else {
                            showEmptyUI(getString(R.string.error_chat_no_walk))
                        }
                    } else {
                        showEmptyUI(getString(R.string.error_chat_no_walk))
                    }
                }
                override fun onFailure(call: Call<List<CitaRequest>>, t: Throwable) {
                    showEmptyUI(getString(R.string.error_conexion_servidor))
                }
            })
        } else {
            RetrofitClient.instance.obtenerCitasDueno(email).enqueue(object : Callback<List<CitaRequest>> {
                override fun onResponse(call: Call<List<CitaRequest>>, response: Response<List<CitaRequest>>) {
                    if (response.isSuccessful) {
                        allChats = response.body()?.filter { it.estado == "ACEPTADO" || it.estado == "EN_CURSO" } ?: emptyList()
                        if (allChats.isNotEmpty()) {
                            showChatUI()
                        } else {
                            showEmptyUI(getString(R.string.chat_owner_wait))
                        }
                    } else {
                        showEmptyUI(getString(R.string.error_fetch_citas))
                    }
                }
                override fun onFailure(call: Call<List<CitaRequest>>, t: Throwable) {
                    showEmptyUI(getString(R.string.error_connection))
                }
            })
        }
    }

    private fun showChatUI() {
        findViewById<View>(R.id.layoutChatList).visibility = View.VISIBLE
        findViewById<View>(R.id.layoutEmptyChat).visibility = View.GONE
        adapter.updateItems(allChats)
    }

    private fun showEmptyUI(description: String) {
        findViewById<View>(R.id.layoutChatList).visibility = View.GONE
        findViewById<View>(R.id.layoutEmptyChat).visibility = View.VISIBLE
        findViewById<TextView>(R.id.tvEmptyDescription).text = description
    }
}
