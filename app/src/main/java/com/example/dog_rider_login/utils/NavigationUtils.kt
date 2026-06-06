package com.example.dog_rider_login.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import com.example.dog_rider_login.ChatActivity
import com.example.dog_rider_login.HomeDuenoActivity
import com.example.dog_rider_login.HomePaseadorActivity
import com.example.dog_rider_login.PaseosActivity
import com.example.dog_rider_login.R
import com.example.dog_rider_login.SolicitarCitaActivity

object NavigationUtils {

    fun configurarNavegacion(activity: Activity) {
        val sessionManager = SessionManager(activity)
        val esPaseador = sessionManager.isWalker()

        // Botones comunes para ambos roles
        val navInicio = activity.findViewById<LinearLayout>(R.id.navInicio) ?: activity.findViewById<LinearLayout>(R.id.btnHome)
        val navPaseos = activity.findViewById<LinearLayout>(R.id.navCitas) ?: activity.findViewById<LinearLayout>(R.id.btnPaseos)
        val navChat = activity.findViewById<LinearLayout>(R.id.navChat) ?: activity.findViewById<LinearLayout>(R.id.btnChat)

        navInicio?.setOnClickListener {
            if (activity !is HomeDuenoActivity && activity !is HomePaseadorActivity) {
                val intent = if (esPaseador) {
                    Intent(activity, HomePaseadorActivity::class.java)
                } else {
                    Intent(activity, HomeDuenoActivity::class.java)
                }
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                activity.startActivity(intent)
            }
        }

        navPaseos?.setOnClickListener {
            val targetClass = if (esPaseador) PaseosActivity::class.java else SolicitarCitaActivity::class.java
            if (activity.javaClass != targetClass) {
                val intent = Intent(activity, targetClass)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                activity.startActivity(intent)
            }
        }

        navChat?.setOnClickListener {
            if (activity !is ChatActivity) {
                val intent = Intent(activity, ChatActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                activity.startActivity(intent)
            }
        }
    }
}
