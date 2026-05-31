package com.example.dog_rider_login

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ChatDetalleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detalle)

        val backButton = findViewById<Button>(R.id.backButton)

        val ownerName = findViewById<TextView>(R.id.ownerName)

        val nombre = intent.getStringExtra("nombre")

        ownerName.text = nombre

        backButton.setOnClickListener {

            finish()

        }

    }
}