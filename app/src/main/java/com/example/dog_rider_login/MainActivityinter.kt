package com.example.dog_rider_login

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivityinter : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // DOG ACTIVO

        val dogImage = findViewById<ImageView>(R.id.dogImage)

        val dogName = findViewById<TextView>(R.id.dogName)
        val dogBreed = findViewById<TextView>(R.id.dogBreed)
        val dogHour = findViewById<TextView>(R.id.dogHour)

        dogName.text = DogData.activeDogName
        dogBreed.text = DogData.activeDogBreed
        dogHour.text = DogData.activeDogHour

        dogImage.setImageResource(DogData.activeDogImage)

        // GPS

        val gpsButton = findViewById<Button>(R.id.gpsButton)
        val gpsStatus = findViewById<TextView>(R.id.gpsStatus)

        gpsButton.setOnClickListener {

            val builder = AlertDialog.Builder(this)

            builder.setTitle("GPS")
            builder.setMessage("¿Activar GPS en tiempo real?")

            builder.setPositiveButton("Sí") { _, _ ->

                gpsStatus.text = "✅ GPS activado"

                Toast.makeText(
                    this,
                    "GPS conectado correctamente 📍",
                    Toast.LENGTH_SHORT
                ).show()

            }

            builder.setNegativeButton("No") { _, _ ->

                gpsStatus.text = "❌ GPS desconectado"

                Toast.makeText(
                    this,
                    "GPS desactivado",
                    Toast.LENGTH_SHORT
                ).show()

            }

            builder.show()

        }

        // BOTON PASEOS

        val btnPaseos = findViewById<LinearLayout>(R.id.btnPaseos)

        btnPaseos.setOnClickListener {

            val intent = Intent(this, PaseosActivity::class.java)
            startActivity(intent)

        }

        // BOTON CHAT

        val btnChat = findViewById<LinearLayout>(R.id.btnChat)

        btnChat.setOnClickListener {

            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)

        }

    }
}