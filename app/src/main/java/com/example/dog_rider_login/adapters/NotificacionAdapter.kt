package com.example.dog_rider_login.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dog_rider_login.R
import com.example.dog_rider_login.models.Notificacion

class NotificacionAdapter(private val listaNotificaciones: List<Notificacion>) :
    RecyclerView.Adapter<NotificacionAdapter.NotificacionViewHolder>() {

    class NotificacionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitulo: TextView = view.findViewById(R.id.tvTituloNotificacion)
        val tvMensaje: TextView = view.findViewById(R.id.tvMensajeNotificacion)
        val tvHora: TextView = view.findViewById(R.id.tvHoraNotificacion)
        val ivIcono: ImageView = view.findViewById(R.id.ivIconoNotificacion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notificacion, parent, false)
        return NotificacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificacionViewHolder, position: Int) {
        val notificacion = listaNotificaciones[position]
        holder.tvTitulo.text = notificacion.titulo
        holder.tvMensaje.text = notificacion.mensaje
        holder.tvHora.text = notificacion.hora

        // Cambiar icono según el tipo
        when (notificacion.tipo) {
            "mensaje" -> holder.ivIcono.setImageResource(android.R.drawable.stat_notify_chat)
            "alerta" -> holder.ivIcono.setImageResource(android.R.drawable.stat_sys_warning)
            else -> holder.ivIcono.setImageResource(android.R.drawable.ic_popup_reminder)
        }
    }

    override fun getItemCount(): Int = listaNotificaciones.size
}
