package com.example.dog_rider_login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        // Esperar 2 segundos y redirigir según el estado de la sesión
        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val email = sharedPref.getString("user_email", null)
            val esPaseador = sharedPref.getBoolean("user_is_walker", false)

            val intent = if (email == null) {
                // Si no hay sesión, ir al Login
                Intent(this, MainActivity::class.java)
            } else {
                // Si hay sesión, ir al Home correspondiente
                if (esPaseador) {
                    Intent(this, HomePaseadorActivity::class.java)
                } else {
                    Intent(this, HomeDuenoActivity::class.java)
                }
            }
            
            startActivity(intent)
            finish()
        }, 2000) // 2000ms = 2 segundos
    }
}
