package com.example.dog_rider_login

import android.content.Context
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
import com.google.android.material.navigation.NavigationView

class HomePaseadorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_paseador)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayoutPaseador)
        val navView = findViewById<NavigationView>(R.id.navViewPaseador)
        val btnMenu = findViewById<ImageView>(R.id.btnMenuPaseador)

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

        // DOG ACTIVO

        val dogImage = findViewById<ImageView>(R.id.dogImage)

        val dogName = findViewById<TextView>(R.id.dogName)
        val dogBreed = findViewById<TextView>(R.id.dogBreed)
        val dogHour = findViewById<TextView>(R.id.dogHour)

        dogName.text = DogData.activeDogName
        dogBreed.text = DogData.activeDogBreed
        dogHour.text = DogData.activeDogHour

        dogImage.setImageResource(DogData.activeDogImage)

        // GPS

        val gpsButton = findViewById<Button>(R.id.gpsButton)
        val gpsStatus = findViewById<TextView>(R.id.gpsStatus)

        gpsButton.setOnClickListener {

            val builder = AlertDialog.Builder(this)

            builder.setTitle("GPS")
            builder.setMessage("¿Activar GPS en tiempo real?")

            builder.setPositiveButton("Sí") { _, _ ->

                gpsStatus.text = "✅ GPS activado"

                Toast.makeText(
                    this,
                    "GPS conectado correctamente 📍",
                    Toast.LENGTH_SHORT
                ).show()

            }

            builder.setNegativeButton("No") { _, _ ->

                gpsStatus.text = "❌ GPS desconectado"

                Toast.makeText(
                    this,
                    "GPS desactivado",
                    Toast.LENGTH_SHORT
                ).show()

            }

            builder.show()

        }

        // BOTON PASEOS

        val btnPaseos = findViewById<LinearLayout>(R.id.btnPaseos)

        btnPaseos.setOnClickListener {

            val intent = Intent(this, PaseosActivity::class.java)
            startActivity(intent)

        }

        // BOTON CHAT

        val btnChat = findViewById<LinearLayout>(R.id.btnChat)

        btnChat.setOnClickListener {

            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)

        }

    }

    override fun onResume() {
        super.onResume()
        // Refrescar datos del menú lateral al volver de Ajustes de Perfil
        val navView = findViewById<NavigationView>(R.id.navViewPaseador)
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
            .setMessage("¿Estás seguro de que deseas salir de tu cuenta de paseador?")
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