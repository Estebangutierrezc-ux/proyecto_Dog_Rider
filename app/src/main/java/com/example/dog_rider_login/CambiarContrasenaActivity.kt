package com.example.dog_rider_login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dog_rider_login.network.RetrofitClient
import com.example.dog_rider_login.network.models.AuthResponse
import com.example.dog_rider_login.network.models.ChangePasswordRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CambiarContrasenaActivity : AppCompatActivity() {

    private lateinit var emailRecuperado: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cambiar_contrasena)

        // Ajustar margenes de diseño
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Recibir el email que el usuario ingreso en la pantalla anterior
        emailRecuperado = intent.getStringExtra("EMAIL") ?: ""

        val btnVolver = findViewById<ImageButton>(R.id.btnVolverRecuperar)
        val etCodigo = findViewById<EditText>(R.id.etCodigoVerificacion)
        val etNuevaClave = findViewById<EditText>(R.id.etNuevaClave)
        val etConfirmarClave = findViewById<EditText>(R.id.etConfirmarClave)
        val btnActualizar = findViewById<Button>(R.id.btnActualizarClave)

        btnVolver.setOnClickListener {
            finish()
        }

        // Al presionar actualizar, validar el codigo y las nuevas claves
        btnActualizar.setOnClickListener {
            val codigo = etCodigo.text.toString().trim()
            val clave = etNuevaClave.text.toString().trim()
            val confirmar = etConfirmarClave.text.toString().trim()

            // Resetear iconos de error para refrescar la validacion
            etCodigo.error = null
            etNuevaClave.error = null
            etConfirmarClave.error = null

            var hayError = false

            // Validacion de codigo: obligatorio, minimo 6 digitos, maximo 6
            if (codigo.isEmpty()) {
                etCodigo.error = "Ingrese el código de verificación"
                hayError = true
            } else if (codigo.length != 6) {
                etCodigo.error = "El código debe tener exactamente 6 dígitos"
                hayError = true
            }

            // Validar seguridad de la nueva clave (Nivel 2): obligatorio, min 8, max 30, mayuscula y numero
            if (clave.isEmpty()) {
                etNuevaClave.error = "Campo obligatorio"
                hayError = true
            } else if (clave.length < 8) {
                etNuevaClave.error = "Mínimo 8 caracteres"
                hayError = true
            } else if (clave.length > 30) {
                etNuevaClave.error = "Máximo 30 caracteres"
                hayError = true
            } else if (!clave.any { it.isUpperCase() }) {
                etNuevaClave.error = "Falta una mayúscula"
                hayError = true
            } else if (!clave.any { it.isDigit() }) {
                etNuevaClave.error = "Falta un número"
                hayError = true
            }

            // Validar que la confirmacion coincida exactamente
            if (confirmar.isEmpty()) {
                etConfirmarClave.error = "Confirme su contraseña"
                hayError = true
            } else if (clave != confirmar) {
                etConfirmarClave.error = "Las contraseñas no coinciden"
                hayError = true
            }

            // Si todo esta validado, mandar a actualizar la BD Oracle
            if (!hayError) {
                actualizarContrasena(codigo, clave)
            }
        }
    }

    // Funcion que envia la peticion de cambio de contraseña al servidor PHP
    private fun actualizarContrasena(codigo: String, nuevaClave: String) {
        val request = ChangePasswordRequest(emailRecuperado, codigo, nuevaClave)

        RetrofitClient.instance.updatePassword(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@CambiarContrasenaActivity, "¡Contraseña actualizada exitosamente!", Toast.LENGTH_SHORT).show()
                    
                    // Navegar al Login (MainActivity) y limpiar el stack de actividades
                    val intent = Intent(this@CambiarContrasenaActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    val msg = response.body()?.message ?: "Error al actualizar"
                    Toast.makeText(this@CambiarContrasenaActivity, msg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(this@CambiarContrasenaActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
