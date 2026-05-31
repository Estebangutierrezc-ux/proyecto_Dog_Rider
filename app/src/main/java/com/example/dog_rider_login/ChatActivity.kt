package com.example.dog_rider_login

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // HOME

        val btnHome = findViewById<LinearLayout>(R.id.btnHome)

        btnHome.setOnClickListener {

            startActivity(Intent(this, MainActivityinter::class.java))

        }

        // PASEOS

        val btnPaseos = findViewById<LinearLayout>(R.id.btnPaseos)

        btnPaseos.setOnClickListener {

            startActivity(Intent(this, PaseosActivity::class.java))

        }

        // CHAT MAX

        val maxChat = findViewById<LinearLayout>(R.id.maxChat)

        maxChat.setOnClickListener {

            val intent = Intent(this, ChatDetalleActivity::class.java)

            intent.putExtra("nombre", "Dueño de Max")

            startActivity(intent)

        }

        // CHAT LUNA

        val lunaChat = findViewById<LinearLayout>(R.id.lunaChat)

        lunaChat.setOnClickListener {

            val intent = Intent(this, ChatDetalleActivity::class.java)

            intent.putExtra("nombre", "Dueño de Luna")

            startActivity(intent)

        }

        // CHAT ROCKY

        val rockyChat = findViewById<LinearLayout>(R.id.rockyChat)

        rockyChat.setOnClickListener {

            val intent = Intent(this, ChatDetalleActivity::class.java)

            intent.putExtra("nombre", "Dueño de Rocky")

            startActivity(intent)

        }

    }
}