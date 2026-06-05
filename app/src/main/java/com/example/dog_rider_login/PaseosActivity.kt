package com.example.dog_rider_login

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dog_rider_login.adapters.PaseoPendienteAdapter
import com.example.dog_rider_login.network.RetrofitClient
import com.example.dog_rider_login.network.models.AuthResponse
import com.example.dog_rider_login.network.models.AceptarPaseoRequest
import com.example.dog_rider_login.network.models.CitaRequest
import com.example.dog_rider_login.utils.NavigationUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaseosActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            val rvPaseos = findViewById<RecyclerView>(R.id.rvPaseosPendientes)
            val tvSinPaseos = findViewById<TextView>(R.id.tvSinPaseos)
            cargarPaseosReales(rvPaseos, tvSinPaseos)
            handler.postDelayed(this, 5000) // Refrescar cada 5 segundos
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paseos)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        
        // Navigation Bar Inferior
        NavigationUtils.configurarNavegacion(this)

        backButton.setOnClickListener { finish() }
        
        findViewById<RecyclerView>(R.id.rvPaseosPendientes).layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }

    private fun cargarPaseosReales(recyclerView: RecyclerView, emptyView: TextView) {
        RetrofitClient.instance.obtenerPaseosPendientes().enqueue(object : Callback<List<CitaRequest>> {
            override fun onResponse(call: Call<List<CitaRequest>>, response: Response<List<CitaRequest>>) {
                if (response.isSuccessful) {
                    val lista = response.body()?.filter { it.estado == "PENDIENTE" } ?: emptyList()
                    if (lista.isNotEmpty()) {
                        recyclerView.adapter = PaseoPendienteAdapter(lista) { paseo ->
                            confirmarAceptarPaseo(paseo)
                        }
                        recyclerView.visibility = android.view.View.VISIBLE
                        emptyView.visibility = android.view.View.GONE
                    } else {
                        recyclerView.visibility = android.view.View.GONE
                        emptyView.visibility = android.view.View.VISIBLE
                    }
                } else {
                    Toast.makeText(this@PaseosActivity, "Error al obtener paseos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<CitaRequest>>, t: Throwable) {
                Toast.makeText(this@PaseosActivity, "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun confirmarAceptarPaseo(paseo: CitaRequest) {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val emailPaseador = sharedPref.getString("user_email", "") ?: ""

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirmar Paseo")
            .setMessage("¿Deseas aceptar el paseo de ${paseo.mascota}?")
            .setPositiveButton("Sí, aceptar") { _, _ ->
                val request = AceptarPaseoRequest(
                    citaId = paseo.id ?: 0,
                    paseadorEmail = emailPaseador
                )
                
                RetrofitClient.instance.aceptarPaseo(request).enqueue(object : Callback<AuthResponse> {
                    override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(this@PaseosActivity, "Paseo aceptado. ¡Ve por ${paseo.mascota}!", Toast.LENGTH_LONG).show()
                            finish()
                        }
                    }
                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                        Toast.makeText(this@PaseosActivity, "Fallo al conectar", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
