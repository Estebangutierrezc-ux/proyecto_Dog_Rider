package com.example.dog_rider_login

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dog_rider_login.network.RetrofitClient
import com.example.dog_rider_login.network.models.AuthResponse
import com.example.dog_rider_login.network.models.RecoverRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecuperarContrasenaActivity : AppCompatActivity() {
    
    // Variable para manejar el cronometro de reenvio de correo
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recuperar_contrasena)

        // Configurar margenes de pantalla segura
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener componentes de la interfaz
        val btnVolver = findViewById<ImageButton>(R.id.btnVolverLogin)
        val etCorreo = findViewById<EditText>(R.id.etCorreoRecuperar)
        val btnEnviar = findViewById<Button>(R.id.btnEnviarEnlace)
        val tvContador = findViewById<TextView>(R.id.tvContadorRecuperar)

        // Cerrar esta pantalla y volver al Login
        btnVolver.setOnClickListener {
            finish()
        }

        // Al hacer clic en enviar, validar correo y llamar al servicio
        btnEnviar.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            
            etCorreo.error = null

            // Validacion de correo: obligatorio, maximo 30 caracteres y formato real
            if (correo.isEmpty()) {
                etCorreo.error = "Campo obligatorio"
            } else if (correo.length > 30) {
                etCorreo.error = "Máximo 30 caracteres"
            } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                etCorreo.error = "Correo inválido"
            } else {
                // Solicitar el codigo real al servidor PHP
                solicitarRecuperacion(correo, btnEnviar, tvContador)
            }
        }
    }

    // Funcion que pide al servidor que envie el correo de recuperacion
    private fun solicitarRecuperacion(email: String, boton: Button, contador: TextView) {
        boton.isEnabled = false // Bloquear el boton temporalmente
        val request = RecoverRequest(email)

        RetrofitClient.instance.recoverPassword(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@RecuperarContrasenaActivity, 
                        "Enlace enviado. Revise su bandeja de entrada.", Toast.LENGTH_LONG).show()
                    
                    // Activar el cronometro de 60 segundos
                    iniciarContador(boton, contador)
                    
                    // Abrir la pantalla para ingresar el codigo recibido
                    val intent = Intent(this@RecuperarContrasenaActivity, CambiarContrasenaActivity::class.java)
                    intent.putExtra("EMAIL", email)
                    startActivity(intent)
                } else {
                    boton.isEnabled = true
                    val msg = response.body()?.message ?: "Error al enviar el enlace"
                    Toast.makeText(this@RecuperarContrasenaActivity, msg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                boton.isEnabled = true
                Toast.makeText(this@RecuperarContrasenaActivity, 
                    "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Logica del contador regresivo para evitar spam de correos
    private fun iniciarContador(boton: Button, contador: TextView) {
        contador.visibility = View.VISIBLE
        timer?.cancel()
        
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                contador.text = "Reenviar en: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                contador.visibility = View.GONE
                boton.isEnabled = true // Desbloquear boton al terminar el tiempo
            }
        }.start()
    }

    // Limpiar el timer si la actividad se cierra
    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
