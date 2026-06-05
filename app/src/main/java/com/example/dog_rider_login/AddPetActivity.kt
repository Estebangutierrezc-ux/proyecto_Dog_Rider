package com.example.dog_rider_login

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.dog_rider_login.local.BaseDatosLocal
import com.example.dog_rider_login.local.entities.MascotaLocal
import com.example.dog_rider_login.utils.NavigationUtils
import kotlinx.coroutines.launch
import org.json.JSONObject

class AddPetActivity : AppCompatActivity() {

    private lateinit var imgMascota: ImageView
    private lateinit var btnSubirImagen: Button
    private lateinit var btnGuardar: Button

    private lateinit var etNombre: EditText
    private lateinit var etRaza: EditText
    private lateinit var etEdad: EditText
    private lateinit var etComentarios: EditText
    private lateinit var tvCharCounter: TextView

    private lateinit var spGenero: Spinner

    private val PICK_IMAGE = 100
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pet)

        // Navigation Bar Inferior
        NavigationUtils.configurarNavegacion(this)

        // Inicializar vistas
        imgMascota = findViewById(R.id.imgMascota)
        btnSubirImagen = findViewById(R.id.btnSubirImagen)
        btnGuardar = findViewById(R.id.btnGuardar)
        etNombre = findViewById(R.id.etNombre)
        etRaza = findViewById(R.id.etRaza)
        etEdad = findViewById(R.id.etEdad)
        etComentarios = findViewById(R.id.etComentarios)
        tvCharCounter = findViewById(R.id.tvCharCounterAddPet)
        spGenero = findViewById(R.id.spGenero)

        // Filtro para prohibir números en Nombre y Raza
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
        etRaza.filters = arrayOf(filtroLetras, InputFilter.LengthFilter(30))

        // Contador de caracteres para comentarios
        etComentarios.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0
                tvCharCounter.text = getString(R.string.counter_150, currentLength)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Configurar Spinner de Género con el nuevo item_spinner para espaciado
        val opcionesGenero = arrayOf(getString(R.string.select_option), "Macho", "Hembra")
        val adapter = ArrayAdapter(this, R.layout.item_spinner, opcionesGenero)
        adapter.setDropDownViewResource(R.layout.item_spinner)
        spGenero.adapter = adapter

        btnSubirImagen.setOnClickListener {
            abrirGaleria()
        }

        btnGuardar.setOnClickListener {
            validarYGuardar()
        }
    }

    private fun validarYGuardar() {
        val nombre = etNombre.text.toString().trim()
        val raza = etRaza.text.toString().trim()
        val edad = etEdad.text.toString().trim()
        val genero = spGenero.selectedItem.toString()
        val comentarios = etComentarios.text.toString().trim()

        var hayError = false

        if (nombre.isEmpty()) {
            etNombre.error = getString(R.string.label_nombre_placeholder)
            hayError = true
        } else if (nombre.length < 2) {
            etNombre.error = getString(R.string.min_letras, 2)
            hayError = true
        }

        if (raza.isEmpty()) {
            etRaza.error = getString(R.string.label_raza_placeholder)
            hayError = true
        }

        if (edad.isEmpty()) {
            etEdad.error = getString(R.string.label_edad_placeholder)
            hayError = true
        }

        if (genero == getString(R.string.select_option)) {
            Toast.makeText(this, getString(R.string.error_select_genero), Toast.LENGTH_SHORT).show()
            hayError = true
        }

        if (hayError) return

        // Proceder a guardar
        btnGuardar.isEnabled = false
        guardarMascotaOnline(nombre, raza, edad, genero, comentarios)
    }

    private fun guardarMascotaOnline(nombre: String, raza: String, edad: String, genero: String, comentarios: String) {
        // Obtenemos la URL centralizada de ngrok
        val url = "https://manual-celibacy-tannery.ngrok-free.dev/dog_rider_api/guardar_mascota.php"

        val stringRequest = object : StringRequest(
            Method.POST,
            url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val success = jsonResponse.getBoolean("success")

                    if (success) {
                        // Guardar en la base de datos local SOLO si Oracle aceptó el dato
                        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                        val emailLogueado = sharedPref.getString("user_email", "") ?: ""

                        lifecycleScope.launch {
                            val nuevaMascota = MascotaLocal(
                                duenoEmail = emailLogueado,
                                nombre = nombre,
                                raza = raza,
                                edad = edad,
                                genero = genero,
                                notas = comentarios
                            )
                            BaseDatosLocal.obtenerInstancia(this@AddPetActivity)
                                .mascotaDao()
                                .insertarMascota(nuevaMascota)

                            Toast.makeText(this@AddPetActivity, getString(R.string.msg_mascota_guardada), Toast.LENGTH_LONG).show()
                            finish()
                        }
                    } else {
                        btnGuardar.isEnabled = true
                        val errorMsg = jsonResponse.optString("message", "Error desconocido en Oracle")
                        Toast.makeText(this, "Fallo Oracle: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    btnGuardar.isEnabled = true
                    Toast.makeText(this, "Error al leer respuesta del servidor", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                btnGuardar.isEnabled = true
                Toast.makeText(this, "Error de red: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                val emailLogueado = sharedPref.getString("user_email", "") ?: ""

                params["dueno_email"] = emailLogueado
                params["nombre"] = nombre
                params["raza"] = raza
                params["edad"] = edad
                params["genero"] = genero
                params["comentarios"] = comentarios
                return params
            }

            // AÑADIR ESTO: Para que ngrok no bloquee la App
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["ngrok-skip-browser-warning"] = "true"
                return headers
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            imgMascota.setImageURI(imageUri)
        }
    }
}
