package com.example.dog_rider_login

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class AddPetActivity : AppCompatActivity() {

    private lateinit var imgMascota: ImageView
    private lateinit var btnSubirImagen: Button
    private lateinit var btnGuardar: Button

    private lateinit var etNombre: EditText
    private lateinit var etRaza: EditText
    private lateinit var etEdad: EditText
    private lateinit var etComentarios: EditText

    private lateinit var spGenero: Spinner

    private val PICK_IMAGE = 100
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pet)

        imgMascota = findViewById(R.id.imgMascota)
        btnSubirImagen = findViewById(R.id.btnSubirImagen)
        btnGuardar = findViewById(R.id.btnGuardar)

        etNombre = findViewById(R.id.etNombre)
        etRaza = findViewById(R.id.etRaza)
        etEdad = findViewById(R.id.etEdad)
        etComentarios = findViewById(R.id.etComentarios)

        spGenero = findViewById(R.id.spGenero)

        val opcionesGenero = arrayOf(
            "Seleccionar",
            "Macho",
            "Hembra"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            opcionesGenero
        )

        spGenero.adapter = adapter

        btnSubirImagen.setOnClickListener {
            abrirGaleria()
        }

        btnGuardar.setOnClickListener {
            guardarMascota()
        }
    }

    private fun guardarMascota() {

        val nombre = etNombre.text.toString()
        val raza = etRaza.text.toString()
        val edad = etEdad.text.toString()
        val genero = spGenero.selectedItem.toString()
        val comentarios = etComentarios.text.toString()

        val url = "http://10.0.2.2/mascotas/guardar_mascota.php"

        val stringRequest = object : StringRequest(
            Request.Method.POST,
            url,

            Response.Listener {

                Toast.makeText(
                    this,
                    "Mascota guardada",
                    Toast.LENGTH_LONG
                ).show()

            },

            Response.ErrorListener {

                Toast.makeText(
                    this,
                    "Error de conexión",
                    Toast.LENGTH_LONG
                ).show()

            }

        ) {

            override fun getParams(): MutableMap<String, String> {

                val params = HashMap<String, String>()

                params["nombre"] = nombre
                params["raza"] = raza
                params["edad"] = edad
                params["genero"] = genero
                params["comentarios"] = comentarios

                return params
            }
        }

        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(stringRequest)
    }

    private fun abrirGaleria() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"

        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode == PICK_IMAGE &&
            resultCode == Activity.RESULT_OK &&
            data != null
        ) {

            imageUri = data.data
            imgMascota.setImageURI(imageUri)
        }
    }
}