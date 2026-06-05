package com.example.dog_rider_login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dog_rider_login.adapters.HistorialAdapter
import com.example.dog_rider_login.network.RetrofitClient
import com.example.dog_rider_login.network.models.AuthResponse
import com.example.dog_rider_login.network.models.CitaRequest
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomePaseadorActivity : AppCompatActivity() {

    private var paseoActivoActual: CitaRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_paseador)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayoutPaseador)
        val navView = findViewById<NavigationView>(R.id.navViewPaseador)
        val btnMenu = findViewById<ImageView>(R.id.btnMenuPaseador)
        val cardPaseoActivo = findViewById<View>(R.id.cardPaseoActivo)

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val headerView = navView.getHeaderView(0)
        val tvEmailHeader = headerView.findViewById<TextView>(R.id.tvUserEmailHeader)
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val emailUser = sharedPref.getString("user_email", "usuario@ejemplo.com")
        tvEmailHeader.text = emailUser

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                }
                R.id.nav_politica -> {
                    mostrarPoliticaPrivacidad()
                }
                R.id.nav_logout -> {
                    confirmarCerrarSesion()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        actualizarDatosNavegacion(navView)

        cardPaseoActivo.setOnClickListener {
            paseoActivoActual?.let { paseo ->
                val intent = Intent(this, DetalleMascotaActivity::class.java)
                intent.putExtra("id", paseo.id)
                intent.putExtra("estado", paseo.estado ?: "ACEPTADO")
                intent.putExtra("nombre", paseo.mascota)
                intent.putExtra("raza", paseo.raza ?: "Mascota")
                intent.putExtra("edad", paseo.edad ?: "N/A")
                intent.putExtra("duracion", paseo.duracion)
                intent.putExtra("personalidad", paseo.notas)
                intent.putExtra("hora", paseo.hora)
                intent.putExtra("precio", paseo.precio)
                intent.putExtra("dueno", paseo.duenoNombre ?: paseo.usuarioEmail)
                startActivity(intent)
            }
        }

        val dogImage = findViewById<ImageView>(R.id.dogImage)
        val dogName = findViewById<TextView>(R.id.dogName)
        val dogBreed = findViewById<TextView>(R.id.dogBreed)
        val dogHour = findViewById<TextView>(R.id.dogHour)

        cargarPaseoActivo(dogName, dogBreed, dogHour, dogImage)
        cargarHistorialReal()

        val gpsButton = findViewById<Button>(R.id.gpsButton)
        val gpsStatus = findViewById<TextView>(R.id.gpsStatus)

        gpsButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("GPS")
            builder.setMessage("¿Activar GPS en tiempo real?")
            builder.setPositiveButton("Sí") { _, _ ->
                gpsStatus.text = getString(R.string.gps_conectado)
                Toast.makeText(this, "GPS conectado correctamente 📍", Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("No") { _, _ ->
                gpsStatus.text = getString(R.string.gps_desconectado)
                Toast.makeText(this, "GPS desactivado", Toast.LENGTH_SHORT).show()
            }
            builder.show()
        }

        findViewById<LinearLayout>(R.id.btnPaseos).setOnClickListener {
            startActivity(Intent(this, PaseosActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnChat).setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }

    private fun cargarPaseoActivo(tvNombre: TextView, tvRaza: TextView, tvHora: TextView, ivFoto: ImageView) {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val email = sharedPref.getString("user_email", "") ?: ""

        RetrofitClient.instance.obtenerPaseoActivo(email).enqueue(object : Callback<CitaRequest> {
            override fun onResponse(call: Call<CitaRequest>, response: Response<CitaRequest>) {
                val body = response.body()
                if (response.isSuccessful && body?.id != null && body.id != 0) {
                    paseoActivoActual = body
                    tvNombre.text = body.mascota
                    tvRaza.text = if (body.estado == "EN_CURSO") {
                        getString(R.string.formato_en_curso, body.duracion)
                    } else {
                        body.duracion
                    }
                    tvHora.text = getString(R.string.formato_hora_emoji, body.hora)
                    ivFoto.setImageResource(R.drawable.app_logo)
                } else {
                    paseoActivoActual = null
                    tvNombre.text = getString(R.string.sin_paseo_activo)
                    tvRaza.text = getString(R.string.aceptar_para_comenzar)
                    tvHora.text = getString(R.string.hora_vacia)
                    ivFoto.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
            override fun onFailure(call: Call<CitaRequest>, t: Throwable) {
                paseoActivoActual = null
            }
        })
    }

    private fun cargarHistorialReal() {
        val rvHistorial = findViewById<RecyclerView>(R.id.rvHistorialPaseos)
        val tvSinHistorial = findViewById<TextView>(R.id.tvSinHistorial)
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val email = sharedPref.getString("user_email", "") ?: ""

        RetrofitClient.instance.obtenerHistorialPaseador(email).enqueue(object : Callback<List<CitaRequest>> {
            override fun onResponse(call: Call<List<CitaRequest>>, response: Response<List<CitaRequest>>) {
                if (response.isSuccessful) {
                    val lista = response.body() ?: emptyList()
                    if (lista.isNotEmpty()) {
                        rvHistorial.layoutManager = LinearLayoutManager(this@HomePaseadorActivity)
                        rvHistorial.adapter = HistorialAdapter(lista) { item ->
                            confirmarEliminarHistorial(item)
                        }
                        rvHistorial.visibility = View.VISIBLE
                        tvSinHistorial.visibility = View.GONE
                    } else {
                        rvHistorial.visibility = View.GONE
                        tvSinHistorial.visibility = View.VISIBLE
                    }
                }
            }
            override fun onFailure(call: Call<List<CitaRequest>>, t: Throwable) {}
        })
    }

    override fun onResume() {
        super.onResume()
        val navView = findViewById<NavigationView>(R.id.navViewPaseador)
        actualizarDatosNavegacion(navView)
        
        val dogImage = findViewById<ImageView>(R.id.dogImage)
        val dogName = findViewById<TextView>(R.id.dogName)
        val dogBreed = findViewById<TextView>(R.id.dogBreed)
        val dogHour = findViewById<TextView>(R.id.dogHour)
        cargarPaseoActivo(dogName, dogBreed, dogHour, dogImage)
        cargarHistorialReal()
    }

    private fun actualizarDatosNavegacion(navView: NavigationView) {
        val headerView = navView.getHeaderView(0)
        val tvNombreHeader = headerView.findViewById<TextView>(R.id.tvUserNameHeader)
        val tvEmailHeader = headerView.findViewById<TextView>(R.id.tvUserEmailHeader)

        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val nombre = sharedPref.getString("user_name", "")
        val apellido = sharedPref.getString("user_lastname", "")
        val emailUser = sharedPref.getString("user_email", "usuario@ejemplo.com")

        if (!nombre.isNullOrEmpty()) {
            tvNombreHeader.text = getString(R.string.formato_nombre_completo, nombre, apellido)
        }
        tvEmailHeader.text = emailUser
    }

    private fun mostrarPoliticaPrivacidad() {
        AlertDialog.Builder(this)
            .setTitle("Política de Privacidad")
            .setMessage("En DogRider protegemos tus datos y los de tu mascota.")
            .setPositiveButton("Entendido", null)
            .show()
    }

    private fun confirmarEliminarHistorial(paseo: CitaRequest) {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar registro?")
            .setMessage("¿Borrar el paseo de ${paseo.mascota}?")
            .setPositiveButton("Eliminar") { _, _ ->
                val request = com.example.dog_rider_login.network.models.AceptarPaseoRequest(
                    citaId = paseo.id ?: 0,
                    paseadorEmail = ""
                )
                RetrofitClient.instance.eliminarHistorial(request).enqueue(object : Callback<AuthResponse> {
                    override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(this@HomePaseadorActivity, "Registro borrado", Toast.LENGTH_SHORT).show()
                            cargarHistorialReal()
                        }
                    }
                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {}
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarCerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Deseas salir?")
            .setPositiveButton("Sí, salir") { _, _ ->
                val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                sharedPref.edit { clear() }
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
