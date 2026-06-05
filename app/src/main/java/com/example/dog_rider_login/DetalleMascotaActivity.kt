package com.example.dog_rider_login

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dog_rider_login.network.RetrofitClient
import com.example.dog_rider_login.network.models.AceptarPaseoRequest
import com.example.dog_rider_login.network.models.AuthResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetalleMascotaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_mascota)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val startWalkButton = findViewById<Button>(R.id.startWalkButton)
        val payButton = findViewById<Button>(R.id.payButton)

        val dogImage = findViewById<ImageView>(R.id.dogImage)
        val dogName = findViewById<TextView>(R.id.dogName)
        val dogBreed = findViewById<TextView>(R.id.dogBreed)
        val dogAge = findViewById<TextView>(R.id.dogAge)
        val dogPersonality = findViewById<TextView>(R.id.dogPersonality)
        val dogHour = findViewById<TextView>(R.id.dogHour)
        val dogPrice = findViewById<TextView>(R.id.dogPrice)
        val tvDueno = findViewById<TextView>(R.id.tvDuenoEmail)

        // CAPTURAR DATOS DEL INTENT
        val idCita = intent.getIntExtra("id", 0)
        val estadoActual = intent.getStringExtra("estado") ?: "ACEPTADO"
        val nombre = intent.getStringExtra("nombre") ?: "Mascota"
        val raza = intent.getStringExtra("raza") ?: "Desconocida"
        val edad = intent.getStringExtra("edad") ?: "N/A"
        val duracion = intent.getStringExtra("duracion") ?: "N/A"
        val notas = intent.getStringExtra("personalidad") ?: getString(R.string.sin_notas)
        val hora = intent.getStringExtra("hora") ?: "--:--"
        val precio = intent.getStringExtra("precio") ?: "$0"
        val emailDueno = intent.getStringExtra("dueno") ?: "Desconocido"

        // MOSTRAR DATOS
        dogName.text = nombre
        dogBreed.text = getString(R.string.label_raza_detalle, raza)
        dogAge.text = getString(R.string.label_edad_detalle, edad)
        dogPersonality.text = getString(R.string.label_notas_detalle, notas)
        dogHour.text = getString(R.string.label_hora_detalle, hora, duracion)
        dogPrice.text = precio
        tvDueno.text = getString(R.string.label_dueno_detalle, emailDueno)

        dogImage.setImageResource(R.drawable.app_logo)

        // LOGICA DE BOTON DINAMICO
        startWalkButton.text = if (estadoActual == "EN_CURSO") {
            getString(R.string.btn_finalizar_paseo_text)
        } else {
            getString(R.string.btn_iniciar_paseo)
        }

        payButton.visibility = View.GONE
        backButton.setOnClickListener { finish() }

        startWalkButton.setOnClickListener {
            if (idCita != 0) {
                if (estadoActual == "EN_CURSO") {
                    finalizarPaseoReal(idCita, nombre)
                } else {
                    iniciarPaseoReal(idCita, nombre)
                }
            }
        }
    }

    private fun iniciarPaseoReal(id: Int, nombre: String) {
        val request = AceptarPaseoRequest(citaId = id, paseadorEmail = "")
        RetrofitClient.instance.iniciarPaseo(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                val success = response.isSuccessful && (response.body()?.success == true)
                if (success) {
                    Toast.makeText(this@DetalleMascotaActivity, "¡Paseo iniciado con $nombre! 🐶", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(this@DetalleMascotaActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun finalizarPaseoReal(id: Int, nombre: String) {
        val request = AceptarPaseoRequest(citaId = id, paseadorEmail = "")
        RetrofitClient.instance.finalizarPaseo(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                val success = response.isSuccessful && (response.body()?.success == true)
                if (success) {
                    Toast.makeText(this@DetalleMascotaActivity, "¡Paseo con $nombre finalizado! ✅", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(this@DetalleMascotaActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
