package com.example.dog_rider_login

import android.os.Bundle
import android.text.InputFilter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.dog_rider_login.network.RetrofitClient
import com.example.dog_rider_login.network.models.AuthResponse
import com.example.dog_rider_login.network.models.UpdateProfileRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PerfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        val btnBack = findViewById<ImageButton>(R.id.btnBackProfile)
        val etNombre = findViewById<EditText>(R.id.etProfileNombre)
        val etApellido = findViewById<EditText>(R.id.etProfileApellido)
        val etTelefono = findViewById<EditText>(R.id.etProfileTelefono)
        val tvEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val btnGuardar = findViewById<Button>(R.id.btnActualizarPerfil)

        // Filtro para prohibir números y símbolos en tiempo real (solo letras y espacios)
        val filtroLetras = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                val char = source[i]
                if (!(char.isLetter() || char == ' ' || char == 'á' || char == 'é' || char == 'í' || char == 'ó' || char == 'ú' ||
                    char == 'Á' || char == 'É' || char == 'Í' || char == 'Ó' || char == 'Ú' || char == 'ñ' || char == 'Ñ')) {
                    return@InputFilter ""
                }
            }
            null
        }

        etNombre.filters = arrayOf(filtroLetras, InputFilter.LengthFilter(30))
        etApellido.filters = arrayOf(filtroLetras, InputFilter.LengthFilter(30))

        // Filtro para el teléfono (Solo números y el signo +, máximo 15 caracteres)
        val filtroTelefono = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                val char = source[i]
                if (!char.isDigit() && char != '+') {
                    return@InputFilter ""
                }
            }
            null
        }
        etTelefono.filters = arrayOf(filtroTelefono, InputFilter.LengthFilter(15))

        // Cargar datos actuales desde SharedPreferences
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val nombreActual = sharedPref.getString("user_name", "")
        val apellidoActual = sharedPref.getString("user_lastname", "")
        val emailActual = sharedPref.getString("user_email", "")
        val telefonoActual = sharedPref.getString("user_phone", "")

        // Mostrar en la interfaz
        etNombre.setText(nombreActual)
        etApellido.setText(apellidoActual)
        etTelefono.setText(telefonoActual)
        tvEmail.text = emailActual

        btnBack.setOnClickListener { finish() }

        btnGuardar.setOnClickListener {
            val nuevoNombre = etNombre.text.toString().trim()
            val nuevoApellido = etApellido.text.toString().trim()
            val nuevoTelefono = etTelefono.text.toString().trim()

            // Resetear errores
            etNombre.error = null
            etApellido.error = null
            etTelefono.error = null

            var hayError = false
            val regexNombres = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$".toRegex()

            // Validar Nombre
            if (nuevoNombre.isEmpty()) {
                etNombre.error = "Campo obligatorio"
                hayError = true
            } else if (nuevoNombre.length < 2) {
                etNombre.error = "Mínimo 2 caracteres"
                hayError = true
            } else if (!nuevoNombre.matches(regexNombres)) {
                etNombre.error = "Solo se permiten letras"
                hayError = true
            }

            // Validar Apellido
            if (nuevoApellido.isEmpty()) {
                etApellido.error = "Campo obligatorio"
                hayError = true
            } else if (nuevoApellido.length < 2) {
                etApellido.error = "Mínimo 2 caracteres"
                hayError = true
            } else if (!nuevoApellido.matches(regexNombres)) {
                etApellido.error = "Solo se permiten letras"
                hayError = true
            }

            // Validar Teléfono
            if (nuevoTelefono.isEmpty()) {
                etTelefono.error = "Campo obligatorio"
                hayError = true
            } else if (nuevoTelefono.length < 8) {
                etTelefono.error = "Mínimo 8 dígitos"
                hayError = true
            }

            if (hayError) return@setOnClickListener

            // Deshabilitar botón para evitar múltiples clics
            btnGuardar.isEnabled = false
            Toast.makeText(this, getString(R.string.msg_actualizando_perfil), Toast.LENGTH_SHORT).show()

            // 1. Intentar actualizar en Oracle (Cloud)
            val request = UpdateProfileRequest(emailActual ?: "", nuevoNombre, nuevoApellido, nuevoTelefono)
            
            RetrofitClient.instance.updateProfile(request).enqueue(
                object : Callback<AuthResponse> {
                    override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                        btnGuardar.isEnabled = true
                        if (response.isSuccessful && (response.body()?.success == true)) {
                            // 2. Si se guardó en Oracle, actualizamos localmente
                            sharedPref.edit {
                                putString("user_name", nuevoNombre)
                                putString("user_lastname", nuevoApellido)
                                putString("user_phone", nuevoTelefono)
                            }
                            Toast.makeText(this@PerfilActivity, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            // Mostrar el error exacto que devuelve el servidor
                            val msg = response.body()?.message ?: getString(R.string.error_servidor_codigo, response.code())
                            Toast.makeText(this@PerfilActivity, getString(R.string.error_fallo_msg, msg), Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                        btnGuardar.isEnabled = true
                        Toast.makeText(this@PerfilActivity, "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                },
            )
        }
    }
}
