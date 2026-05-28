package com.example.dog_rider_login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dog_rider_login.network.RetrofitClient
import com.example.dog_rider_login.network.models.AuthResponse
import com.example.dog_rider_login.network.models.LoginRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    
    // Metodo principal que se ejecuta al abrir la pantalla de Login
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        // Ajustar el diseño para que no choque con la barra de estado del celular
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Capturar los elementos del diseño (XML) para usarlos en el codigo
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        // Escuchar cuando el usuario hace clic en el boton de entrar
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Limpiar avisos rojos de errores anteriores para refrescar el intento
            etEmail.error = null
            etPassword.error = null

            var hayError = false

            // Validar Correo: no vacio, formato real y maximo 30 caracteres
            if (email.isEmpty()) {
                etEmail.error = "Campo obligatorio"
                hayError = true
            } else if (email.length > 30) {
                etEmail.error = "Máximo 30 caracteres"
                hayError = true
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Formato de correo inválido"
                hayError = true
            }

            // Validar Contraseña: no vacia y maximo 30 caracteres
            if (password.isEmpty()) {
                etPassword.error = "Campo obligatorio"
                hayError = true
            } else if (password.length > 30) {
                etPassword.error = "Máximo 30 caracteres"
                hayError = true
            }

            // Si paso la validacion visual, pregunto al servidor Oracle
            if (!hayError) {
                btnLogin.isEnabled = false // Deshabilitar para evitar múltiples clics
                iniciarSesion(email, password, etEmail, etPassword)
            }
        }

        // Abrir la pantalla para recuperar la clave
        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, RecuperarContrasenaActivity::class.java)
            startActivity(intent)
        }

        // Abrir la pantalla para crear una cuenta nueva
        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // Funcion encargada de enviar los datos al PHP para verificar el acceso
    private fun iniciarSesion(email: String, clave: String, fieldEmail: EditText, fieldPass: EditText) {
        val request = LoginRequest(email, clave)
        
        // Conectar con el servidor usando Retrofit
        RetrofitClient.instance.login(request).enqueue(
            object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                val btnLogin = findViewById<Button>(R.id.btnLogin)
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    
                    // Si el servidor confirma que el usuario existe y la clave es correcta
                    if (authResponse?.success == true) {
                        // Guardar los datos del perfil del usuario en SharedPreferences
                        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                        sharedPref.edit().apply {
                            putString("user_email", email)
                            putString("user_name", authResponse.nombre ?: "")
                            putString("user_lastname", authResponse.apellido ?: "")
                            putString("user_phone", authResponse.telefono ?: "")
                            apply()
                        }

                        Toast.makeText(this@MainActivity, "¡Hola de nuevo!", Toast.LENGTH_SHORT).show()
                        
                        // Mandar al usuario a su pantalla principal (Home Dueño)
                        val intent = Intent(this@MainActivity, HomeDuenoActivity::class.java)
                        startActivity(intent)
                        finish() 
                    } else {
                        btnLogin.isEnabled = true // Re-habilitar si falla
                        // SI LAS CREDENCIALES SON INCORRECTAS: Marco los campos en rojo
                        fieldEmail.error = "Revise sus credenciales"
                        fieldPass.error = "Revise sus credenciales"
                        Toast.makeText(this@MainActivity, "Email o contraseña incorrectos", Toast.LENGTH_LONG).show()
                    }
                } else {
                    btnLogin.isEnabled = true // Re-habilitar si falla
                    Toast.makeText(this@MainActivity, "Error en el servidor", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                val btnLogin = findViewById<Button>(R.id.btnLogin)
                btnLogin.isEnabled = true // Re-habilitar si falla
                Toast.makeText(this@MainActivity, "Falla de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
