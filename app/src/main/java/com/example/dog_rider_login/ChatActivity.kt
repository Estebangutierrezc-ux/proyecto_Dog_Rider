package com.example.dog_rider_login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.dog_rider_login.network.RetrofitClient
import com.example.dog_rider_login.network.models.CitaRequest
import com.example.dog_rider_login.utils.NavigationUtils
import com.example.dog_rider_login.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        sessionManager = SessionManager(this)
        NavigationUtils.configurarNavegacion(this)

        checkActiveChats()
    }

    private fun checkActiveChats() {
        val email = sessionManager.getUserEmail() ?: ""
        val isWalker = sessionManager.isWalker()

        if (isWalker) {
            RetrofitClient.instance.obtenerPaseoActivo(email).enqueue(object : Callback<CitaRequest> {
                override fun onResponse(call: Call<CitaRequest>, response: Response<CitaRequest>) {
                    val paseo = response.body()
                    if (response.isSuccessful && paseo?.id != null && paseo.id != 0) {
                        showChatUI(
                            idCita = paseo.id,
                            receptorEmail = paseo.usuarioEmail ?: "",
                            nombreReceptor = paseo.duenoNombre ?: "Dueño de Mascota",
                            foto = paseo.foto
                        )
                    } else {
                        showEmptyUI("Podrás hablar con el dueño una vez que aceptes un paseo.")
                    }
                }
                override fun onFailure(call: Call<CitaRequest>, t: Throwable) {
                    showEmptyUI("Error al conectar con el servidor.")
                }
            })
        } else {
            RetrofitClient.instance.obtenerCitasDueno(email).enqueue(object : Callback<List<CitaRequest>> {
                override fun onResponse(call: Call<List<CitaRequest>>, response: Response<List<CitaRequest>>) {
                    if (response.isSuccessful) {
                        val citaActiva = response.body()?.find { it.estado == "ACEPTADO" || it.estado == "EN_CURSO" }
                        if (citaActiva != null && citaActiva.paseadorEmail != null) {
                            showChatUI(
                                idCita = citaActiva.id ?: 0,
                                receptorEmail = citaActiva.paseadorEmail,
                                nombreReceptor = "Paseador de ${citaActiva.mascota}",
                                foto = citaActiva.foto
                            )
                        } else {
                            showEmptyUI("Podrás hablar con tu paseador una vez que acepte tu solicitud.")
                        }
                    } else {
                        showEmptyUI("Error al obtener tus citas.")
                    }
                }
                override fun onFailure(call: Call<List<CitaRequest>>, t: Throwable) {
                    showEmptyUI("Error de conexión.")
                }
            })
        }
    }

    private fun showChatUI(idCita: Int, receptorEmail: String, nombreReceptor: String, foto: String?) {
        findViewById<View>(R.id.layoutChatList).visibility = View.VISIBLE
        findViewById<View>(R.id.layoutEmptyChat).visibility = View.GONE

        val tvName = findViewById<TextView>(R.id.tvChatUserName)
        val ivMascota = findViewById<ImageView>(R.id.ivChatMascota)
        val cvChat = findViewById<View>(R.id.cvActiveChat)

        tvName.text = nombreReceptor

        // Cargar imagen (Avatar o Foto)
        if (!foto.isNullOrEmpty()) {
            if (foto.startsWith("avatar_")) {
                val idRes = resources.getIdentifier(foto, "drawable", packageName)
                ivMascota.setImageResource(if (idRes != 0) idRes else R.drawable.app_logo)
            } else {
                val url = "https://manual-celibacy-tannery.ngrok-free.dev/dog_rider_api/uploads/$foto"
                Glide.with(this).load(url).centerCrop().into(ivMascota)
            }
        }

        cvChat.setOnClickListener {
            val intent = Intent(this, ChatDetalleActivity::class.java)
            intent.putExtra("id_cita", idCita)
            intent.putExtra("receptor_email", receptorEmail)
            intent.putExtra("nombre", nombreReceptor)
            startActivity(intent)
        }
    }

    private fun showEmptyUI(description: String) {
        findViewById<View>(R.id.layoutChatList).visibility = View.GONE
        findViewById<View>(R.id.layoutEmptyChat).visibility = View.VISIBLE
        findViewById<TextView>(R.id.tvEmptyDescription).text = description
    }
}
