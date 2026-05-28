package com.example.dog_rider_login

import android.os.Bundle
import android.text.InputFilter
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.dog_rider_login.local.BaseDatosLocal
import com.example.dog_rider_login.local.entities.UsuarioLocal
import com.example.dog_rider_login.network.RetrofitClient
import com.example.dog_rider_login.network.models.AuthResponse
import com.example.dog_rider_login.network.models.RegisterRequest
import com.example.dog_rider_login.sync.SincronizacionWorker
import com.example.dog_rider_login.utils.RedUtil
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etApellido = findViewById<EditText>(R.id.etApellido)
        val etTelefono = findViewById<EditText>(R.id.etTelefono)
        val etEmail = findViewById<EditText>(R.id.etRegisterEmail)
        val etPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val spinnerWalker = findViewById<Spinner>(R.id.spinnerWalker)
        val btnCreateAccount = findViewById<Button>(R.id.btnCreateAccount)
        val btnBack = findViewById<ImageButton>(R.id.btnBackToLogin)
        val tvErrorMessages = findViewById<TextView>(R.id.tvErrorMessages)

        // Filtros de entrada (Iguales al Perfil para consistencia)
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
        val filtroTelefono = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                val char = source[i]
                if (!char.isDigit() && char != '+') return@InputFilter ""
            }
            null
        }

        etNombre.filters = arrayOf(filtroLetras, InputFilter.LengthFilter(30))
        etApellido.filters = arrayOf(filtroLetras, InputFilter.LengthFilter(30))
        etTelefono.filters = arrayOf(filtroTelefono, InputFilter.LengthFilter(15))

        btnBack.setOnClickListener {
            finish()
        }

        val options = arrayOf("Seleccionar", "Sí", "No")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerWalker.adapter = adapter

        btnCreateAccount.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val apellido = etApellido.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val isWalkerText = spinnerWalker.selectedItem.toString()

            etNombre.error = null
            etApellido.error = null
            etTelefono.error = null
            etEmail.error = null
            etPassword.error = null
            tvErrorMessages.visibility = View.GONE

            var hayError = false

            if (nombre.isEmpty()) { etNombre.error = "Campo obligatorio"; hayError = true }
            else if (nombre.length < 2) { etNombre.error = "Mínimo 2 letras"; hayError = true }

            if (apellido.isEmpty()) { etApellido.error = "Campo obligatorio"; hayError = true }
            else if (apellido.length < 2) { etApellido.error = "Mínimo 2 letras"; hayError = true }

            if (telefono.isEmpty()) { etTelefono.error = "Campo obligatorio"; hayError = true }
            else if (telefono.length < 8) { etTelefono.error = "Mínimo 8 dígitos"; hayError = true }

            if (email.isEmpty()) { etEmail.error = "Campo obligatorio"; hayError = true }
            else if (email.length > 30) { etEmail.error = "Máximo 30 caracteres"; hayError = true }
            else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.error = "Email inválido"; hayError = true }

            if (password.isEmpty()) { etPassword.error = "Campo obligatorio"; hayError = true }
            else if (password.length < 8) { etPassword.error = "Mínimo 8 caracteres"; hayError = true }
            else if (!password.any { it.isUpperCase() }) { etPassword.error = "Falta mayúscula"; hayError = true }
            else if (!password.any { it.isDigit() }) { etPassword.error = "Falta un número"; hayError = true }
            
            if (isWalkerText == "Seleccionar") {
                tvErrorMessages.text = getString(R.string.error_select_walker)
                tvErrorMessages.visibility = View.VISIBLE
                hayError = true
            }

            if (!hayError) {
                // Deshabilitar botón para evitar múltiples clics
                btnCreateAccount.isEnabled = false

                // Verificar si hay internet para decidir como guardar
                if (RedUtil.hayInternet(this)) {
                    registrarUsuarioOnline(nombre, apellido, telefono, email, password, isWalkerText == "Sí")
                } else {
                    guardarUsuarioLocalmente(nombre, apellido, telefono, email, password, isWalkerText == "Sí")
                }
            }
        }
    }

    private fun registrarUsuarioOnline(
        nombre: String,
        apellido: String,
        telefono: String,
        email: String,
        clave: String,
        esPaseador: Boolean,
    ) {
        val request = RegisterRequest(nombre, apellido, telefono, email, clave, esPaseador)
        val btnCreateAccount = findViewById<Button>(R.id.btnCreateAccount)
        
        RetrofitClient.instance.register(request).enqueue(
            object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        // Guardar datos del usuario recién registrado para el perfil
                        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                        sharedPref.edit().apply {
                            putString("user_name", nombre)
                            putString("user_lastname", apellido)
                            putString("user_email", email)
                            putString("user_phone", telefono)
                            apply()
                        }

                        Toast.makeText(this@RegisterActivity, "¡Cuenta creada con éxito!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        btnCreateAccount.isEnabled = true // Re-habilitar si falla
                        val msg = response.body()?.message ?: "Error en el servidor"
                        Toast.makeText(this@RegisterActivity, msg, Toast.LENGTH_LONG).show()
                        // Si falla el servidor, lo guardamos localmente por si acaso
                        guardarUsuarioLocalmente(nombre, apellido, telefono, email, clave, esPaseador)
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    btnCreateAccount.isEnabled = true // Re-habilitar si falla
                    Toast.makeText(this@RegisterActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
                    // Si falla la conexión, guardamos localmente
                    guardarUsuarioLocalmente(nombre, apellido, telefono, email, clave, esPaseador)
                }
            },
        )
    }

    private fun guardarUsuarioLocalmente(nombre: String, apellido: String, telefono: String, 
                                         email: String, clave: String, esPaseador: Boolean) {
        val usuario = UsuarioLocal(
            nombre = nombre,
            apellido = apellido,
            telefono = telefono,
            email = email,
            clave = clave,
            esPaseador = esPaseador
        )

        lifecycleScope.launch {
            val db = BaseDatosLocal.obtenerInstancia(this@RegisterActivity)
            db.usuarioDao().insertarUsuario(usuario)
            
            // Guardar también en SharedPreferences para que el perfil se vea aunque no haya internet
            val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
            sharedPref.edit().apply {
                putString("user_name", nombre)
                putString("user_lastname", apellido)
                putString("user_email", email)
                putString("user_phone", telefono)
                apply()
            }

            programarSincronizacion()
            
            Toast.makeText(this@RegisterActivity, 
                "Sin internet. Se guardó localmente y se enviará luego.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun programarSincronizacion() {
        // Reglas para que el trabajador se active: solo cuando haya internet
        val restricciones = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val tarea = OneTimeWorkRequestBuilder<SincronizacionWorker>()
            .setConstraints(restricciones)
            .build()

        WorkManager.getInstance(this).enqueue(tarea)
    }
}
