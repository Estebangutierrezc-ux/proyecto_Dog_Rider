package com.example.dog_rider_login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dog_rider_login.adapters.HistorialAdapter
import com.example.dog_rider_login.adapters.PaseoAsignadoAdapter
import com.example.dog_rider_login.network.RetrofitClient
import com.example.dog_rider_login.network.models.AuthResponse
import com.example.dog_rider_login.network.models.CitaRequest
import com.example.dog_rider_login.utils.NavigationUtils
import com.example.dog_rider_login.utils.SessionManager
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomePaseadorActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var listaPaseosActivos = listOf<CitaRequest>()
    private var estaExpandidoPaseos = false
    private var listaHistorialCompleta = listOf<CitaRequest>()
    private var estaExpandidoHistorial = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_home_paseador)

            sessionManager = SessionManager(this)
            
            // SEGURIDAD: Verificar sesión activa
            if (sessionManager.getUserEmail().isNullOrEmpty()) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return
            }

            val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayoutPaseador)
            val navView = findViewById<NavigationView>(R.id.navViewPaseador)
            val btnMenu = findViewById<ImageView>(R.id.btnMenuPaseador)

            btnMenu?.setOnClickListener {
                drawerLayout?.openDrawer(GravityCompat.START)
            }

            val headerView = navView?.getHeaderView(0)
            val tvEmailHeader = headerView?.findViewById<TextView>(R.id.tvUserEmailHeader)
            tvEmailHeader?.text = sessionManager.getUserEmail()

            navView?.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_perfil -> startActivity(Intent(this, PerfilActivity::class.java))
                    R.id.nav_politica -> mostrarPoliticaPrivacidad()
                    R.id.nav_logout -> confirmarCerrarSesion()
                }
                drawerLayout?.closeDrawer(GravityCompat.START)
                true
            }

            setupChatButton()
            setupToggles()
            setupHistoryToggle()

            findViewById<LinearLayout>(R.id.btnPaseos)?.setOnClickListener {
                startActivity(Intent(this, PaseosActivity::class.java))
            }

            NavigationUtils.configurarNavegacion(this)

        } catch (e: Exception) {
            Log.e("HOME_PASEADOR", "Error inicial: ${e.message}")
        }
    }

    private fun setupChatButton() {
        findViewById<LinearLayout>(R.id.btnChat)?.setOnClickListener {
            if (listaPaseosActivos.isNotEmpty()) {
                val paseo = listaPaseosActivos[0] 
                val intent = Intent(this, ChatDetalleActivity::class.java).apply {
                    putExtra("id_cita", paseo.id)
                    putExtra("receptor_email", paseo.usuarioEmail)
                    putExtra("nombre", getString(R.string.formato_chat_title, paseo.duenoNombre))
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, getString(R.string.error_chat_no_walk), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupHistoryToggle() {
        findViewById<TextView>(R.id.tvVerTodasHistorial)?.setOnClickListener {
            estaExpandidoHistorial = !estaExpandidoHistorial
            
            val layoutPaseosHoy = findViewById<LinearLayout>(R.id.layoutPaseosHoyPaseador)
            
            if (estaExpandidoHistorial) {
                layoutPaseosHoy?.visibility = View.GONE
            } else {
                layoutPaseosHoy?.visibility = View.VISIBLE
            }

            (it as TextView).text = if (estaExpandidoHistorial)
                getString(R.string.link_ver_menos) else getString(R.string.link_ver_todas)
            actualizarListaHistorial()
        }
    }

    private fun setupToggles() {
        findViewById<TextView>(R.id.tvVerTodasPaseos)?.setOnClickListener {
            estaExpandidoPaseos = !estaExpandidoPaseos
            
            val layoutHistorial = findViewById<LinearLayout>(R.id.layoutHistorialPaseador)
            val tvTitulo = findViewById<TextView>(R.id.tvTituloPaseosAsignados)

            if (estaExpandidoPaseos) {
                layoutHistorial?.visibility = View.GONE
                tvTitulo?.text = "Todos mis Paseos"
            } else {
                layoutHistorial?.visibility = View.VISIBLE
                tvTitulo?.text = "Mis Paseos para hoy"
            }

            (it as TextView).text = if (estaExpandidoPaseos) 
                getString(R.string.link_ver_menos) else getString(R.string.link_ver_todas)
            actualizarListaPaseosActivos()
        }
    }

    private fun cargarPaseosActivosReal() {
        val email = sessionManager.getUserEmail() ?: ""
        RetrofitClient.instance.obtenerPaseosActivos(email).enqueue(object : Callback<List<CitaRequest>> {
            override fun onResponse(call: Call<List<CitaRequest>>, response: Response<List<CitaRequest>>) {
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    listaPaseosActivos = body
                    actualizarListaPaseosActivos()
                } else {
                    listaPaseosActivos = emptyList()
                    actualizarListaPaseosActivos()
                }
            }
            override fun onFailure(call: Call<List<CitaRequest>>, t: Throwable) {
                Log.e("API_ERROR", "Error al cargar paseos activos: ${t.message}")
                listaPaseosActivos = emptyList()
                actualizarListaPaseosActivos()
            }
        })
    }

    private fun actualizarListaPaseosActivos() {
        val rv = findViewById<RecyclerView>(R.id.rvPaseosActivos)
        val tvSin = findViewById<TextView>(R.id.tvSinPaseoActivo)
        val tvVerTodas = findViewById<TextView>(R.id.tvVerTodasPaseos)

        if (listaPaseosActivos.isNotEmpty()) {
            val listaAMostrar = if (estaExpandidoPaseos) listaPaseosActivos else listaPaseosActivos.take(2)
            
            rv?.layoutManager = LinearLayoutManager(this)
            rv?.adapter = PaseoAsignadoAdapter(listaAMostrar) { paseo ->
                abrirDetallePaseo(paseo)
            }
            rv?.visibility = View.VISIBLE
            tvSin?.visibility = View.GONE
            
            // Botón visible si hay más de 2 o si ya está expandido (para poder contraer)
            tvVerTodas?.visibility = if (listaPaseosActivos.size > 2 || estaExpandidoPaseos) View.VISIBLE else View.GONE
        } else {
            rv?.visibility = View.GONE
            tvSin?.visibility = View.VISIBLE
            // Si está expandido pero se vació la lista, permitimos contraer para recuperar la vista
            tvVerTodas?.visibility = if (estaExpandidoPaseos) View.VISIBLE else View.GONE
        }
    }

    private fun abrirDetallePaseo(paseo: CitaRequest) {
        val intent = Intent(this, DetalleMascotaActivity::class.java).apply {
            putExtra("id", paseo.id)
            putExtra("estado", paseo.estado ?: "ACEPTADO")
            putExtra("nombre", paseo.mascota)
            putExtra("raza", paseo.raza ?: "Mascota")
            putExtra("edad", paseo.edad ?: "N/A")
            putExtra("duracion", paseo.duracion)
            putExtra("personalidad", paseo.notas)
            putExtra("hora", paseo.hora)
            putExtra("precio", paseo.precio)
            putExtra("dueno", paseo.duenoNombre ?: paseo.usuarioEmail)
            putExtra("foto", paseo.foto)
        }
        startActivity(intent)
    }

    private fun cargarHistorialReal() {
        val email = sessionManager.getUserEmail() ?: ""
        RetrofitClient.instance.obtenerHistorialPaseador(email).enqueue(object : Callback<List<CitaRequest>> {
            override fun onResponse(call: Call<List<CitaRequest>>, response: Response<List<CitaRequest>>) {
                if (response.isSuccessful) {
                    listaHistorialCompleta = response.body() ?: emptyList()
                    actualizarListaHistorial()
                }
            }
            override fun onFailure(call: Call<List<CitaRequest>>, t: Throwable) {}
        })
    }

    private fun actualizarListaHistorial() {
        val rvHistorial = findViewById<RecyclerView>(R.id.rvHistorialPaseos) ?: return
        val tvSinHistorial = findViewById<TextView>(R.id.tvSinHistorial)
        val tvVerTodas = findViewById<TextView>(R.id.tvVerTodasHistorial)

        if (listaHistorialCompleta.isNotEmpty()) {
            val listaAMostrar = if (estaExpandidoHistorial) {
                listaHistorialCompleta
            } else {
                listaHistorialCompleta.take(2)
            }

            rvHistorial.layoutManager = LinearLayoutManager(this)
            rvHistorial.adapter = HistorialAdapter(listaAMostrar) { item ->
                confirmarEliminarHistorial(item)
            }
            rvHistorial.visibility = View.VISIBLE
            tvSinHistorial?.visibility = View.GONE
            
            // Botón visible si hay más de 2 o si ya está expandido (para poder contraer)
            tvVerTodas?.visibility = if (listaHistorialCompleta.size > 2 || estaExpandidoHistorial) View.VISIBLE else View.GONE
        } else {
            rvHistorial.visibility = View.GONE
            tvSinHistorial?.visibility = View.VISIBLE
            
            // Si está expandido pero se vació la lista, permitimos contraer para recuperar la vista
            tvVerTodas?.visibility = if (estaExpandidoHistorial) View.VISIBLE else View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarDatosNavegacion(findViewById(R.id.navViewPaseador))
        cargarPaseosActivosReal()
        cargarHistorialReal()
    }

    private fun actualizarDatosNavegacion(navView: NavigationView?) {
        val headerView = navView?.getHeaderView(0)
        val tvNombreHeader = headerView?.findViewById<TextView>(R.id.tvUserNameHeader)
        val tvEmailHeader = headerView?.findViewById<TextView>(R.id.tvUserEmailHeader)

        tvNombreHeader?.text = getString(R.string.formato_nombre_completo, 
            sessionManager.getUserName(), sessionManager.getUserLastName())
        tvEmailHeader?.text = sessionManager.getUserEmail()
    }

    private fun mostrarPoliticaPrivacidad() {
        AlertDialog.Builder(this)
            .setTitle(R.string.nav_politica)
            .setMessage(R.string.app_name)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun confirmarEliminarHistorial(paseo: CitaRequest) {
        AlertDialog.Builder(this)
            .setTitle(R.string.btn_eliminar)
            .setMessage(getString(R.string.dialog_eliminar_historial, paseo.mascota))
            .setPositiveButton(R.string.btn_eliminar) { _, _ ->
                val request = com.example.dog_rider_login.network.models.AceptarPaseoRequest(
                    citaId = paseo.id ?: 0,
                    paseadorEmail = ""
                )
                RetrofitClient.instance.eliminarHistorial(request).enqueue(object : Callback<AuthResponse> {
                    override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(this@HomePaseadorActivity, getString(R.string.msg_registro_borrado), Toast.LENGTH_SHORT).show()
                            cargarHistorialReal()
                        }
                    }
                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {}
                })
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun confirmarCerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle(R.string.nav_logout)
            .setMessage(R.string.dialog_cerrar_sesion_pregunta)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                sessionManager.logout()
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
