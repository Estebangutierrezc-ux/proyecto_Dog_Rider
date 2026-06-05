package com.example.dog_rider_login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dog_rider_login.adapters.CitaAdapter
import com.example.dog_rider_login.adapters.MascotaAdapter
import com.example.dog_rider_login.adapters.NotificacionAdapter
import com.example.dog_rider_login.local.BaseDatosLocal
import com.example.dog_rider_login.local.entities.CitaLocal
import com.example.dog_rider_login.local.entities.MascotaLocal
import com.example.dog_rider_login.models.Mascota
import com.example.dog_rider_login.models.Notificacion
import com.example.dog_rider_login.utils.NavigationUtils
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeDuenoActivity : AppCompatActivity() {

    private var listaMascotasCompleta = listOf<MascotaLocal>()
    private var estaExpandidoMascotas = false
    
    private var listaCitasCompleta = listOf<CitaLocal>()
    private var estaExpandidoCitas = false

    private var listaNotificaciones = listOf<Notificacion>()

    // Metodo que inicializa la pantalla principal del dueño
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_dueno)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navView = findViewById<NavigationView>(R.id.navView)
        val btnMenu = findViewById<ImageView>(R.id.btnMenu)

        val ivUserProfile = findViewById<ImageView>(R.id.ivUserProfile)
        val rvMascotas = findViewById<RecyclerView>(R.id.rvMascotas)
        val rvCitas = findViewById<RecyclerView>(R.id.rvCitas)

        // Configurar Drawer
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Configurar Info del Header del Drawer
        val headerView = navView.getHeaderView(0)
        val tvEmailHeader = headerView.findViewById<TextView>(R.id.tvUserEmailHeader)
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val emailUser = sharedPref.getString("user_email", "usuario@ejemplo.com")
        tvEmailHeader.text = emailUser

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_perfil -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    startActivity(intent)
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

        val tvVerTodasMascotas = findViewById<TextView>(R.id.tvVerTodasMascotas)
        val tvVerTodasCitas = findViewById<TextView>(R.id.tvVerTodasCitas)
        val tvTituloCitasHeader = findViewById<TextView>(R.id.tvTituloCitasHeader)
        val containerMascotas = findViewById<LinearLayout>(R.id.containerMascotas)
        val btnAnadirMascota = findViewById<android.widget.Button>(R.id.btnAnadirMascota)

        // Configurar Botón Añadir Mascota
        btnAnadirMascota.setOnClickListener {
            val intent = Intent(this, AddPetActivity::class.java)
            startActivity(intent)
        }

        // Elementos de Notificaciones
        val btnNotifications = findViewById<View>(R.id.btnNotificationsContainer)
        val layoutNotificacionesOverlay = findViewById<View>(R.id.layoutNotificacionesOverlay)
        val btnCerrarNotificaciones = findViewById<View>(R.id.btnCerrarNotificaciones)
        val rvNotificaciones = findViewById<RecyclerView>(R.id.rvNotificaciones)
        val tvNotificationBadge = findViewById<TextView>(R.id.tvNotificationBadge)

        // Mock de Notificaciones
        listaNotificaciones = listOf(
            Notificacion(id = 1, titulo = "Mensaje de Juan", mensaje = "Hola, ya llegué por Max!", hora = "14:30", leida = false, tipo = "mensaje"),
            Notificacion(id = 2, titulo = "Paseo Finalizado", mensaje = "Luna ha regresado de su paseo.", hora = "11:15", leida = true, tipo = "info"),
        )

        // Configurar Badge
        if (listaNotificaciones.any { !it.leida }) {
            tvNotificationBadge.text = listaNotificaciones.count { !it.leida }.toString()
            tvNotificationBadge.visibility = View.VISIBLE
        }

        // Lógica de apertura de notificaciones
        btnNotifications.setOnClickListener {
            layoutNotificacionesOverlay.visibility = View.VISIBLE
            rvNotificaciones.layoutManager = LinearLayoutManager(this)
            rvNotificaciones.adapter = NotificacionAdapter(listaNotificaciones)
            tvNotificationBadge.visibility = View.GONE
        }

        btnCerrarNotificaciones.setOnClickListener {
            layoutNotificacionesOverlay.visibility = View.GONE
        }

        // Cargar Mascotas Reales filtradas por el usuario logueado
        lifecycleScope.launch {
            val emailLogueado = sharedPref.getString("user_email", "") ?: ""
            BaseDatosLocal.obtenerInstancia(this@HomeDuenoActivity)
                .mascotaDao()
                .obtenerMascotasPorDuenio(emailLogueado)
                .collectLatest { mascotas ->
                    listaMascotasCompleta = mascotas
                    actualizarListaMascotas(rvMascotas)
                }
        }

        // Cargar Citas Reales filtradas por el usuario logueado
        lifecycleScope.launch {
            val emailLogueado = sharedPref.getString("user_email", "") ?: ""
            BaseDatosLocal.obtenerInstancia(this@HomeDuenoActivity)
                .citaDao()
                .obtenerCitasPorUsuario(emailLogueado)
                .collectLatest { citas ->
                    listaCitasCompleta = citas
                    actualizarListaCitas(rvCitas)
                }
        }

        // Click en Ver Todas Mascotas
        tvVerTodasMascotas.setOnClickListener {
            estaExpandidoMascotas = !estaExpandidoMascotas
            actualizarListaMascotas(rvMascotas)
            tvVerTodasMascotas.text = if (estaExpandidoMascotas) getString(R.string.link_ver_menos) else getString(R.string.link_ver_todas)
        }
        
        // Click en Ver Todas Citas
        tvVerTodasCitas.setOnClickListener {
            estaExpandidoCitas = !estaExpandidoCitas
            
            if (estaExpandidoCitas) {
                // Modo "Ver Todas": Ocultamos mascotas y cambiamos textos
                containerMascotas.visibility = View.GONE
                tvTituloCitasHeader.text = getString(R.string.titulo_todas_citas)
                tvVerTodasCitas.text = getString(R.string.link_ver_menos)
                tvTituloCitasHeader.setTextColor(getColor(R.color.blue_primary))
            } else {
                // Modo normal: Mostramos mascotas de nuevo
                containerMascotas.visibility = View.VISIBLE
                tvTituloCitasHeader.text = getString(R.string.titulo_proximas_citas)
                tvVerTodasCitas.text = getString(R.string.link_ver_todas)
                tvTituloCitasHeader.setTextColor(getColor(R.color.light_gray))
            }
            
            actualizarListaCitas(rvCitas)
        }

        // Navigation Bar Inferior
        NavigationUtils.configurarNavegacion(this)

        // Al hacer clic en la foto de perfil, abrir la pantalla de perfil
        ivUserProfile.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }
        
        // Ajustar el diseño para que el contenido no quede detras de la barra de estado
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        // Refrescar datos del menú lateral al volver de Ajustes de Perfil
        val navView = findViewById<NavigationView>(R.id.navView)
        actualizarDatosNavegacion(navView)
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

    private fun actualizarListaMascotas(recyclerView: RecyclerView) {
        val listaAMostrar = if (estaExpandidoMascotas) {
            listaMascotasCompleta
        } else {
            listaMascotasCompleta.take(3)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MascotaAdapter(listaAMostrar)
    }

    private fun actualizarListaCitas(recyclerView: RecyclerView) {
        val listaAMostrar = if (estaExpandidoCitas) {
            listaCitasCompleta
        } else {
            listaCitasCompleta.take(3)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CitaAdapter(listaAMostrar)
    }

    private fun mostrarPoliticaPrivacidad() {
        AlertDialog.Builder(this)
            .setTitle("Política de Privacidad")
            .setMessage("En DogRider protegemos tus datos y los de tu mascota. Toda la información es utilizada solo para gestionar los paseos.")
            .setPositiveButton("Entendido", null)
            .show()
    }

    private fun confirmarCerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que deseas salir de tu cuenta?")
            .setPositiveButton("Sí, salir") { _, _ ->
                val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                sharedPref.edit {
                    clear()
                }

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
