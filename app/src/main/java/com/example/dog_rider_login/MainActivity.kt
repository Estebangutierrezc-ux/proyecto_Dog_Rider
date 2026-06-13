package com.example.dog_rider_login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.example.dog_rider_login.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var btnLogin: Button? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
            setContentView(R.layout.activity_main)
            
            sessionManager = SessionManager(this)

            val rootView = findViewById<View>(R.id.main)
            rootView?.let { view ->
                ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                    insets
                }
            }

            setupLoginLogic()
            
        } catch (e: Exception) {
            Log.e("LOGIN_ERROR", "Error inicial: ${e.message}")
            Toast.makeText(this, getString(R.string.error_inicio_sistema), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLoginLogic() {
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        btnLogin?.setOnClickListener {
            val email = etEmail?.text.toString().trim()
            val password = etPassword?.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_campos_vacios), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin?.isEnabled = false
            iniciarSesion(email, password)
        }

        findViewById<TextView>(R.id.tvForgotPassword)?.setOnClickListener {
            startActivity(Intent(this, RecuperarContrasenaActivity::class.java))
        }

        findViewById<TextView>(R.id.tvRegister)?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun iniciarSesion(email: String, clave: String) {
        val request = LoginRequest(email, clave)
        RetrofitClient.instance.login(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val auth = response.body()!!
                    sessionManager.saveUserSession(
                        email = email,
                        name = auth.nombre ?: "",
                        lastName = auth.apellido ?: "",
                        phone = auth.telefono ?: "",
                        isWalker = auth.esPaseador ?: false
                    )

                    val intent = if (auth.esPaseador == true) {
                        Intent(this@MainActivity, HomePaseadorActivity::class.java)
                    } else {
                        Intent(this@MainActivity, HomeDuenoActivity::class.java)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    btnLogin?.isEnabled = true
                    Toast.makeText(this@MainActivity, getString(R.string.error_credenciales), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                btnLogin?.isEnabled = true
                Log.e("API_ERROR", "Fallo login: ${t.message}")
                Toast.makeText(this@MainActivity, getString(R.string.error_conexion_servidor), Toast.LENGTH_SHORT).show()
            }
        })
    }
}
