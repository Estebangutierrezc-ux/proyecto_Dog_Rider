package com.example.dog_rider_login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivityañadir : AppCompatActivity() {

    private lateinit var btnAgregarMascota: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainadd)

        btnAgregarMascota = findViewById(R.id.btnAgregarMascota)

        btnAgregarMascota.setOnClickListener {

            val intent = Intent(
                this,
                AddPetActivity::class.java
            )

            startActivity(intent)
        }
    }
}