package com.example.dog_rider_login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dog_rider_login.adapters.CitaAdapter
import com.example.dog_rider_login.adapters.MascotaAdapter
import com.example.dog_rider_login.local.BaseDatosLocal
import com.example.dog_rider_login.local.entities.CitaLocal
import com.example.dog_rider_login.local.entities.MascotaLocal
import com.example.dog_rider_login.network.RetrofitClient
import com.example.dog_rider_login.network.models.AuthResponse
import com.example.dog_rider_login.network.models.CitaRequest
import com.example.dog_rider_login.network.models.MascotaResponse
import com.example.dog_rider_login.utils.NavigationUtils
import com.example.dog_rider_login.utils.SessionManager
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeDuenoActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var listaMascotasCompleta = listOf<MascotaLocal>()
    private var estaExpandidoMascotas = false
    private var listaCitasCompleta = listOf<CitaLocal>()
    private var estaExpandidoCitas = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
            setContentView(R.layout.activity_home_dueno)

            findViewById<View>(R.id.main)?.let { rootView ->
                ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                    insets
                }
            }

            sessionManager = SessionManager(this)
            
            if (sessionManager.getUserEmail().isNullOrEmpty()) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                return
            }

            setupUI()
            setupDataObservers()
            NavigationUtils.configurarNavegacion(this)

        } catch (e: Exception) {
            Log.e("HOME_ERROR", "Error inicial: ${e.message}")
        }
    }

    private fun setupUI() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navView = findViewById<NavigationView>(R.id.navView)
        
        findViewById<ImageView>(R.id.btnMenu)?.setOnClickListener { drawerLayout?.openDrawer(GravityCompat.START) }
        findViewById<ImageView>(R.id.ivUserProfile)?.setOnClickListener { 
            startActivity(Intent(this, PerfilActivity::class.java)) 
        }

        navView?.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_perfil -> startActivity(Intent(this, PerfilActivity::class.java))
                R.id.nav_politica -> mostrarPoliticaPrivacidad()
                R.id.nav_logout -> confirmarCerrarSesion()
            }
            drawerLayout?.closeDrawer(GravityCompat.START)
            true
        }

        findViewById<TextView>(R.id.tvVerTodasMascotas)?.setOnClickListener {
            estaExpandidoMascotas = !estaExpandidoMascotas
            actualizarListaMascotas()
            (it as TextView).text = if (estaExpandidoMascotas) getString(R.string.link_ver_menos) else getString(R.string.link_ver_todas)
        }

        findViewById<TextView>(R.id.tvVerTodasCitas)?.setOnClickListener {
            estaExpandidoCitas = !estaExpandidoCitas
            val container = findViewById<LinearLayout>(R.id.containerMascotas)
            val titulo = findViewById<TextView>(R.id.tvTituloCitasHeader)
            
            if (estaExpandidoCitas) {
                container?.visibility = View.GONE
                titulo?.text = getString(R.string.titulo_todas_citas)
            } else {
                container?.visibility = View.VISIBLE
                titulo?.text = getString(R.string.titulo_proximas_citas)
            }
            actualizarListaCitas()
            (it as TextView).text = if (estaExpandidoCitas) getString(R.string.link_ver_menos) else getString(R.string.link_ver_todas)
        }

        findViewById<Button>(R.id.btnAnadirMascota)?.setOnClickListener {
            startActivity(Intent(this, AddPetActivity::class.java))
        }

        actualizarDatosNavegacion(navView)
    }

    private fun setupDataObservers() {
        val email = sessionManager.getUserEmail() ?: ""
        
        lifecycleScope.launch {
            BaseDatosLocal.obtenerInstancia(this@HomeDuenoActivity).mascotaDao()
                .obtenerMascotasPorDuenio(email).collectLatest {
                    listaMascotasCompleta = it
                    actualizarListaMascotas()
                }
        }

        lifecycleScope.launch {
            BaseDatosLocal.obtenerInstancia(this@HomeDuenoActivity).citaDao()
                .obtenerCitasPorUsuario(email).collectLatest {
                    listaCitasCompleta = it
                    actualizarListaCitas()
                }
        }
    }

    private fun syncMascotasConServidor() {
        val email = sessionManager.getUserEmail() ?: ""
        RetrofitClient.instance.obtenerMascotasDueno(email).enqueue(object : Callback<List<MascotaResponse>> {
            override fun onResponse(call: Call<List<MascotaResponse>>, response: Response<List<MascotaResponse>>) {
                if (response.isSuccessful) {
                    val remotas = response.body() ?: emptyList()
                    lifecycleScope.launch {
                        val db = BaseDatosLocal.obtenerInstancia(this@HomeDuenoActivity)
                        val locales = remotas.map { r ->
                            MascotaLocal(
                                duenoEmail = email,
                                nombre = r.nombre,
                                raza = r.raza,
                                edad = r.edad,
                                genero = r.genero,
                                notas = r.comentarios,
                                foto = r.foto_nombre
                            )
                        }
                        db.mascotaDao().insertarMascotas(locales)
                    }
                }
            }
            override fun onFailure(call: Call<List<MascotaResponse>>, t: Throwable) {
                Log.e("SYNC", "Fallo sincronización mascotas: ${t.message}")
            }
        })
    }

    private fun syncCitasConServidor() {
        val email = sessionManager.getUserEmail() ?: ""
        RetrofitClient.instance.obtenerCitasDueno(email).enqueue(object : Callback<List<CitaRequest>> {
            override fun onResponse(call: Call<List<CitaRequest>>, response: Response<List<CitaRequest>>) {
                if (response.isSuccessful) {
                    val remotas = response.body() ?: emptyList()
                    lifecycleScope.launch {
                        val db = BaseDatosLocal.obtenerInstancia(this@HomeDuenoActivity)
                        val locales = remotas.map { r ->
                            CitaLocal(
                                id = r.id ?: 0,
                                usuarioEmail = email,
                                mascota = r.mascota,
                                fecha = r.fecha,
                                hora = r.hora,
                                duracion = r.duracion,
                                precio = r.precio,
                                notas = r.notas,
                                foto = r.foto,
                                estado = r.estado ?: "PENDIENTE",
                                paseadorEmail = r.paseadorEmail
                            )
                        }
                        db.citaDao().insertarCitas(locales)
                    }
                }
            }
            override fun onFailure(call: Call<List<CitaRequest>>, t: Throwable) {
                Log.e("SYNC", "Fallo sincronización citas: ${t.message}")
            }
        })
    }

    private fun actualizarListaMascotas() {
        val rv = findViewById<RecyclerView>(R.id.rvMascotas) ?: return
        val lista = if (estaExpandidoMascotas) listaMascotasCompleta else listaMascotasCompleta.take(3)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = MascotaAdapter(lista) { confirmarEliminarMascota(it) }
    }

    private fun actualizarListaCitas() {
        val rv = findViewById<RecyclerView>(R.id.rvCitas) ?: return
        val lista = if (estaExpandidoCitas) listaCitasCompleta else listaCitasCompleta.take(3)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = CitaAdapter(lista) { confirmarEliminarCita(it) }
    }

    override fun onResume() {
        super.onResume()
        actualizarDatosNavegacion(findViewById(R.id.navView))
        syncCitasConServidor()
        syncMascotasConServidor()
    }

    private fun actualizarDatosNavegacion(navView: NavigationView?) {
        val header = navView?.getHeaderView(0)
        val tvNombre = header?.findViewById<TextView>(R.id.tvUserNameHeader)
        val tvEmail = header?.findViewById<TextView>(R.id.tvUserEmailHeader)

        tvNombre?.text = getString(
            R.string.formato_nombre_completo,
            sessionManager.getUserName(),
            sessionManager.getUserLastName()
        )
        tvEmail?.text = sessionManager.getUserEmail()
    }

    private fun confirmarEliminarMascota(mascota: MascotaLocal) {
        AlertDialog.Builder(this).setTitle(R.string.btn_eliminar)
            .setMessage(getString(R.string.error_duplicado, mascota.nombre)) // Reutilizamos o creamos uno nuevo si prefieres
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val request = mapOf("nombre" to mascota.nombre, "email" to mascota.duenoEmail)
                RetrofitClient.instance.eliminarMascota(request).enqueue(object : Callback<AuthResponse> {
                    override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            lifecycleScope.launch { 
                                BaseDatosLocal.obtenerInstancia(this@HomeDuenoActivity)
                                    .mascotaDao().eliminarMascotaPorNombre(mascota.nombre, mascota.duenoEmail) 
                            }
                        }
                    }
                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {}
                })
            }.setNegativeButton(android.R.string.cancel, null).show()
    }

    private fun confirmarEliminarCita(cita: CitaLocal) {
        AlertDialog.Builder(this).setTitle(R.string.btn_eliminar)
            .setMessage(R.string.label_notas_especiales)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                RetrofitClient.instance.eliminarCitaDueno(mapOf("citaId" to cita.id)).enqueue(object : Callback<AuthResponse> {
                    override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            lifecycleScope.launch { 
                                BaseDatosLocal.obtenerInstancia(this@HomeDuenoActivity)
                                    .citaDao().eliminarCitaPorId(cita.id) 
                            }
                        }
                    }
                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {}
                })
            }.setNegativeButton(android.R.string.cancel, null).show()
    }

    private fun mostrarPoliticaPrivacidad() {
        AlertDialog.Builder(this).setTitle(R.string.nav_politica).setMessage(R.string.app_name).setPositiveButton(android.R.string.ok, null).show()
    }

    private fun confirmarCerrarSesion() {
        AlertDialog.Builder(this).setTitle(R.string.nav_logout).setMessage(R.string.welcome_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                sessionManager.logout()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }.setNegativeButton(android.R.string.cancel, null).show()
    }
}
