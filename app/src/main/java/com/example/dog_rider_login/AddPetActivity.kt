package com.example.dog_rider_login

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
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
import com.example.dog_rider_login.utils.SessionManager
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class AddPetActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var imgMascota: ImageView
    private lateinit var btnGuardar: Button
    private lateinit var etNombre: EditText
    private lateinit var etRaza: EditText
    private lateinit var etEdad: EditText
    private lateinit var etComentarios: EditText
    private lateinit var tvCharCounter: TextView
    private lateinit var spGenero: Spinner

    private val PICK_IMAGE = 100
    private var encodedImage: String = ""
    private var avatarSeleccionado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_add_pet)

            sessionManager = SessionManager(this)
            NavigationUtils.configurarNavegacion(this)

            // Inicializar vistas
            imgMascota = findViewById(R.id.imgMascota)
            btnGuardar = findViewById(R.id.btnGuardar)
            etNombre = findViewById(R.id.etNombre)
            etRaza = findViewById(R.id.etRaza)
            etEdad = findViewById(R.id.etEdad)
            etComentarios = findViewById(R.id.etComentarios)
            tvCharCounter = findViewById(R.id.tvCharCounterAddPet)
            spGenero = findViewById(R.id.spGenero)

            setupFilters()
            setupButtons()
            
        } catch (e: Exception) {
            Log.e("ADD_PET", "Error en onCreate: ${e.message}")
            Toast.makeText(this, getString(R.string.error_inicio_sistema), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupFilters() {
        val filtroLetras = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                if (!source[i].isLetter() && source[i] != ' ') return@InputFilter ""
            }
            null
        }
        val filtroSeguridad = InputFilter { source, start, end, _, _, _ ->
            val prohibidos = "<>{}[]^|\\"
            for (i in start until end) {
                if (prohibidos.contains(source[i])) return@InputFilter ""
            }
            null
        }

        etNombre.filters = arrayOf(filtroLetras, InputFilter.LengthFilter(30))
        etRaza.filters = arrayOf(filtroLetras, InputFilter.LengthFilter(30))
        etComentarios.filters = arrayOf(filtroSeguridad, InputFilter.LengthFilter(150))

        etComentarios.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvCharCounter.text = getString(R.string.counter_150, s?.length ?: 0)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupButtons() {
        findViewById<View>(R.id.btnAvatar1)?.setOnClickListener { seleccionarAvatar("avatar_mascota1", R.id.ivAvatar1) }
        findViewById<View>(R.id.btnAvatar2)?.setOnClickListener { seleccionarAvatar("avatar_mascota2", R.id.ivAvatar2) }
        findViewById<View>(R.id.btnAvatar3)?.setOnClickListener { seleccionarAvatar("avatar_mascota3", R.id.ivAvatar3) }
        findViewById<Button>(R.id.btnSubirImagen)?.setOnClickListener { abrirGaleria() }
        btnGuardar.setOnClickListener { validarYGuardar() }

        val btnToggle = findViewById<View>(R.id.btnToggleAvatares)
        val container = findViewById<View>(R.id.layoutAvataresContainer)
        val arrow = findViewById<ImageView>(R.id.ivArrowAvatares)

        btnToggle?.setOnClickListener {
            val isVisible = container?.visibility == View.VISIBLE
            container?.visibility = if (isVisible) View.GONE else View.VISIBLE
            arrow?.animate()?.rotation(if (isVisible) 0f else 180f)?.start()
        }
        
        val options = arrayOf(
            getString(R.string.select_option),
            getString(R.string.label_macho),
            getString(R.string.label_hembra)
        )
        val adapter = ArrayAdapter(this, R.layout.item_spinner, options)
        spGenero.adapter = adapter
    }

    private fun seleccionarAvatar(nombre: String, imageViewId: Int) {
        avatarSeleccionado = nombre
        encodedImage = "" 
        val iv = findViewById<ImageView>(imageViewId)
        if (iv != null) {
            imgMascota.setImageDrawable(iv.drawable)
            Toast.makeText(this, getString(R.string.msg_avatar_seleccionado), Toast.LENGTH_SHORT).show()
        }
    }

    private fun validarYGuardar() {
        val nombre = etNombre.text.toString().trim()
        val raza = etRaza.text.toString().trim()
        val edad = etEdad.text.toString().trim()
        val genero = spGenero.selectedItem.toString()
        
        if (nombre.isEmpty() || raza.isEmpty() || edad.isEmpty() || genero == getString(R.string.select_option)) {
            Toast.makeText(this, getString(R.string.error_campos_vacios), Toast.LENGTH_SHORT).show()
            return
        }

        if (encodedImage.isEmpty() && avatarSeleccionado.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_foto_o_avatar), Toast.LENGTH_SHORT).show()
            return
        }

        btnGuardar.isEnabled = false
        guardarMascotaOnline(nombre, raza, edad, genero, etComentarios.text.toString())
    }

    private fun guardarMascotaOnline(nom: String, raz: String, ed: String, gen: String, com: String) {
        val url = "https://manual-celibacy-tannery.ngrok-free.dev/dog_rider_api/guardar_mascota.php"
        val stringRequest = object : StringRequest(Method.POST, url,
            { res ->
                try {
                    val json = JSONObject(res)
                    if (json.optBoolean("success", false)) {
                        guardarLocalmente(nom, raz, ed, gen, com, json.optString("foto_nombre", "default.jpg"))
                    } else {
                        btnGuardar.isEnabled = true
                        val msg = json.optString("message", getString(R.string.error_datos_servidor))
                        Toast.makeText(this, getString(R.string.error_fallo_msg, msg), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    btnGuardar.isEnabled = true
                    Log.e("ADD_PET", "Error JSON: $res")
                    Toast.makeText(this, getString(R.string.error_datos_servidor), Toast.LENGTH_SHORT).show()
                }
            },
            { 
                btnGuardar.isEnabled = true
                Toast.makeText(this, getString(R.string.error_sin_conexion), Toast.LENGTH_SHORT).show() 
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val p = HashMap<String, String>()
                p["dueno_email"] = sessionManager.getUserEmail() ?: ""
                p["nombre"] = nom
                p["raza"] = raz
                p["edad"] = ed
                p["genero"] = gen
                p["comentarios"] = com
                p["foto_base64"] = encodedImage
                p["avatar_nombre"] = avatarSeleccionado
                return p
            }
            override fun getHeaders() = mapOf("ngrok-skip-browser-warning" to "true")
        }
        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun guardarLocalmente(nom: String, raz: String, ed: String, gen: String, com: String, fot: String) {
        val email = sessionManager.getUserEmail() ?: ""
        lifecycleScope.launch {
            try {
                val pet = MascotaLocal(duenoEmail = email, nombre = nom, raza = raz, edad = ed, genero = gen, notas = com, foto = fot)
                BaseDatosLocal.obtenerInstancia(this@AddPetActivity).mascotaDao().insertarMascota(pet)
                Toast.makeText(this@AddPetActivity, getString(R.string.msg_mascota_guardada), Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Log.e("ROOM", "Error: ${e.message}")
                Toast.makeText(this@AddPetActivity, getString(R.string.error_local_db), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            try {
                val uri = data.data
                imgMascota.setImageURI(uri)
                avatarSeleccionado = "" 
                lifecycleScope.launch { 
                    val inputStream = contentResolver.openInputStream(uri!!)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val out = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out)
                    encodedImage = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)
                }
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.error_procesar_imagen), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
