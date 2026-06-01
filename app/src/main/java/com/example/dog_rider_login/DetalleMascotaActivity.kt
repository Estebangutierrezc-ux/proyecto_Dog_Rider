package com.example.dog_rider_login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DetalleMascotaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_mascota)

        val backButton = findViewById<Button>(R.id.backButton)

        val startWalkButton = findViewById<Button>(R.id.startWalkButton)
        val payButton = findViewById<Button>(R.id.payButton)

        val dogImage = findViewById<ImageView>(R.id.dogImage)

        val dogName = findViewById<TextView>(R.id.dogName)
        val dogBreed = findViewById<TextView>(R.id.dogBreed)
        val dogAge = findViewById<TextView>(R.id.dogAge)
        val dogPersonality = findViewById<TextView>(R.id.dogPersonality)
        val dogHour = findViewById<TextView>(R.id.dogHour)
        val dogPrice = findViewById<TextView>(R.id.dogPrice)

        // DATOS

        val nombre = intent.getStringExtra("nombre")
        val raza = intent.getStringExtra("raza")
        val edad = intent.getStringExtra("edad")
        val personalidad = intent.getStringExtra("personalidad")
        val hora = intent.getStringExtra("hora")
        val precio = intent.getStringExtra("precio")

        val imagen = intent.getIntExtra("imagen", R.drawable.rocky)

        // MOSTRAR

        dogName.text = nombre
        dogBreed.text = "🐶 Raza: $raza"
        dogAge.text = "🎂 Edad: $edad"
        dogPersonality.text = "😎 Personalidad: $personalidad"
        dogHour.text = "🕒 Paseo: $hora"
        dogPrice.text = precio

        dogImage.setImageResource(imagen)

        // VOLVER

        backButton.setOnClickListener {

            finish()

        }

        // INICIAR PASEO

        startWalkButton.setOnClickListener {

            DogData.activeDogName = nombre!!
            DogData.activeDogBreed = "$raza • $edad"
            DogData.activeDogHour = "🕒 $hora"
            DogData.activeDogImage = imagen

            Toast.makeText(
                this,
                "Paseo iniciado con $nombre 🐕",
                Toast.LENGTH_SHORT
            ).show()

            startActivity(Intent(this, HomePaseadorActivity::class.java))

        }

        // COBRAR

        payButton.setOnClickListener {

            Toast.makeText(
                this,
                "Pago recibido correctamente 💵",
                Toast.LENGTH_SHORT
            ).show()

        }

    }
}