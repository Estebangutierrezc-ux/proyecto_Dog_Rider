package com.example.dog_rider_login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.dog_rider_login.utils.SessionManager

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val sessionManager = SessionManager(this)

        // Esperar 2 segundos y redirigir según el estado de la sesión
        Handler(Looper.getMainLooper()).postDelayed({
            val email = sessionManager.getUserEmail()
            val esPaseador = sessionManager.isWalker()

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
