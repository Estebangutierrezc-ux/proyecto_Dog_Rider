package com.example.dog_rider_login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class PaseosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paseos)

        // VOLVER

        val backButton = findViewById<Button>(R.id.backButton)

        backButton.setOnClickListener {

            finish()

        }

        // HOME

        val btnHome = findViewById<LinearLayout>(R.id.btnHome)

        btnHome.setOnClickListener {

            startActivity(Intent(this, HomePaseadorActivity::class.java))

        }

        // CHAT

        val btnChat = findViewById<LinearLayout>(R.id.btnChat)

        btnChat.setOnClickListener {

            startActivity(Intent(this, ChatActivity::class.java))

        }

        // ROCKY

        val rockyCard = findViewById<LinearLayout>(R.id.rockyCard)

        rockyCard.setOnClickListener {

            val intent = Intent(this, DetalleMascotaActivity::class.java)

            intent.putExtra("nombre", "Rocky")
            intent.putExtra("raza", "Bulldog")
            intent.putExtra("edad", "4 años")
            intent.putExtra("personalidad", "Juguetón y amigable")
            intent.putExtra("hora", "03:00 PM")
            intent.putExtra("precio", "$12.000 CLP")
            intent.putExtra("imagen", R.drawable.rocky)

            startActivity(intent)

        }

        // LUNA

        val lunaCard = findViewById<LinearLayout>(R.id.lunaCard)

        lunaCard.setOnClickListener {

            val intent = Intent(this, DetalleMascotaActivity::class.java)

            intent.putExtra("nombre", "Luna")
            intent.putExtra("raza", "Beagle")
            intent.putExtra("edad", "2 años")
            intent.putExtra("personalidad", "Tierna y energética")
            intent.putExtra("hora", "05:00 PM")
            intent.putExtra("precio", "$10.000 CLP")
            intent.putExtra("imagen", R.drawable.luna)

            startActivity(intent)

        }

        // MAX

        val maxCard = findViewById<LinearLayout>(R.id.maxCard)

        maxCard.setOnClickListener {

            val intent = Intent(this, DetalleMascotaActivity::class.java)

            intent.putExtra("nombre", "Max")
            intent.putExtra("raza", "Golden Retriever")
            intent.putExtra("edad", "3 años")
            intent.putExtra("personalidad", "Muy obediente y tranquilo")
            intent.putExtra("hora", "06:30 PM")
            intent.putExtra("precio", "$15.000 CLP")
            intent.putExtra("imagen", R.drawable.max)

            startActivity(intent)

        }

    }
}