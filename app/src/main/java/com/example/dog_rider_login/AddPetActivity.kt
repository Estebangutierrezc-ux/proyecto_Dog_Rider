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
    private var encodedImage: String = ""
    private var avatarSeleccionado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pet)

        sessionManager = SessionManager(this)

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

        // Botones de Avatares (Nombres exactos de tus archivos)
        findViewById<View>(R.id.btnAvatar1).setOnClickListener { seleccionarAvatar("avatar_mascota1", R.id.ivAvatar1) }
        findViewById<View>(R.id.btnAvatar2).setOnClickListener { seleccionarAvatar("avatar_mascota2", R.id.ivAvatar2) }
        findViewById<View>(R.id.btnAvatar3).setOnClickListener { seleccionarAvatar("avatar_mascota3", R.id.ivAvatar3) }

        // Filtro letras y espacios
        val filtroLetras = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                if (!source[i].isLetter() && source[i] != ' ') return@InputFilter ""
            }
            null
        }
        // Filtro para evitar caracteres peligrosos en los comentarios (Anti-Hacking)
        val filtroSeguridad = InputFilter { source, start, end, _, _, _ ->
            val simbolosProhibidos = "<>{}[]^|\\"
            for (i in start until end) {
                if (simbolosProhibidos.contains(source[i])) {
                    return@InputFilter ""
                }
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

        val adapter = ArrayAdapter(this, R.layout.item_spinner, arrayOf("Seleccionar", "Macho", "Hembra"))
        spGenero.adapter = adapter

        btnSubirImagen.setOnClickListener { abrirGaleria() }
        btnGuardar.setOnClickListener { validarYGuardar() }

        // LOGICA DESPLEGABLE PARA AVATARES
        val btnToggle = findViewById<View>(R.id.btnToggleAvatares)
        val container = findViewById<View>(R.id.layoutAvataresContainer)
        val arrow = findViewById<ImageView>(R.id.ivArrowAvatares)

        btnToggle.setOnClickListener {
            val nextVisible = container.visibility != View.VISIBLE
            container.visibility = if (nextVisible) View.VISIBLE else View.GONE
            arrow.animate().rotation(if (nextVisible) 180f else 0f).start()
        }
    }

    private fun seleccionarAvatar(nombre: String, imageViewId: Int) {
        avatarSeleccionado = nombre
        encodedImage = "" // Resetear foto si elige avatar
        val ivSelected = findViewById<ImageView>(imageViewId)
        imgMascota.setImageDrawable(ivSelected.drawable)
        Toast.makeText(this, "Avatar seleccionado 🐾", Toast.LENGTH_SHORT).show()
    }

    private fun validarYGuardar() {
        val nombre = etNombre.text.toString().trim()
        val raza = etRaza.text.toString().trim()
        val edad = etEdad.text.toString().trim()
        val genero = spGenero.selectedItem.toString()
        
        var error = false
        if (nombre.isEmpty()) { etNombre.error = "Nombre requerido"; error = true }
        if (raza.isEmpty()) { etRaza.error = "Raza requerida"; error = true }
        if (genero == "Seleccionar") { Toast.makeText(this, "Elige género", Toast.LENGTH_SHORT).show(); error = true }
        if (encodedImage.isEmpty() && avatarSeleccionado.isEmpty()) {
            Toast.makeText(this, "Sube una foto o elige un avatar", Toast.LENGTH_SHORT).show()
            error = true
        }

        if (!error) {
            btnGuardar.isEnabled = false
            guardarMascotaOnline(nombre, raza, edad, genero, etComentarios.text.toString())
        }
    }

    private fun guardarMascotaOnline(nom: String, raz: String, ed: String, gen: String, com: String) {
        val url = "https://manual-celibacy-tannery.ngrok-free.dev/dog_rider_api/guardar_mascota.php"
        val stringRequest = object : StringRequest(Method.POST, url,
            { res ->
                val json = JSONObject(res)
                if (json.getBoolean("success")) {
                    guardarLocalmente(nom, raz, ed, gen, com, json.getString("foto_nombre"))
                } else {
                    btnGuardar.isEnabled = true
                    Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show()
                }
            },
            { btnGuardar.isEnabled = true; Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show() }
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
            val pet = MascotaLocal(duenoEmail = email, nombre = nom, raza = raz, edad = ed, genero = gen, notas = com, foto = fot)
            BaseDatosLocal.obtenerInstancia(this@AddPetActivity).mascotaDao().insertarMascota(pet)
            Toast.makeText(this@AddPetActivity, "¡Mascota guardada con éxito!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun abrirGaleria() {
        startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            imgMascota.setImageURI(imageUri)
            avatarSeleccionado = "" // Resetear avatar si sube foto
            lifecycleScope.launch { 
                try {
                    val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri!!))
                    val out = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
                    encodedImage = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)
                } catch (e: Exception) {
                    Toast.makeText(this@AddPetActivity, "Error al procesar imagen", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
