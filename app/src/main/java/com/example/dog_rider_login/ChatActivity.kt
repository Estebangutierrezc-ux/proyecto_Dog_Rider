package com.example.dog_rider_login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.dog_rider_login.utils.NavigationUtils

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Navigation Bar Inferior
        NavigationUtils.configurarNavegacion(this)

        // CHATS MOCKS
        val rockyChat = findViewById<View>(R.id.rockyChat)
        val lunaChat = findViewById<View>(R.id.lunaChat)

        rockyChat?.setOnClickListener {
            val intent = Intent(this, ChatDetalleActivity::class.java)
            intent.putExtra("nombre", "Dueño de Rocky")
            startActivity(intent)
        }

        lunaChat?.setOnClickListener {
            val intent = Intent(this, ChatDetalleActivity::class.java)
            intent.putExtra("nombre", "Dueño de Luna")
            startActivity(intent)
        }
    }
}
