package com.example.dog_rider_login.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dog_rider_login.R
import com.example.dog_rider_login.local.entities.CitaLocal

class CitaAdapter(
    private val listaCitas: List<CitaLocal>,
    private val onDeleteClick: (CitaLocal) -> Unit
) : RecyclerView.Adapter<CitaAdapter.CitaViewHolder>() {

    class CitaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivMascota: ImageView = view.findViewById(R.id.ivMascotaCita)
        val tvFecha: TextView = view.findViewById(R.id.tvFechaCita)
        val tvTitulo: TextView = view.findViewById(R.id.tvTituloCita)
        val tvDetalles: TextView = view.findViewById(R.id.tvDetallesCita)
        val layoutDetalles: View = view.findViewById(R.id.layoutDetallesCita)
        val ivExpand: ImageView = view.findViewById(R.id.ivExpandIconCita)
        val btnEliminar: View = view.findViewById(R.id.btnEliminarHistorial)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val context = holder.itemView.context
        val cita = listaCitas[position]
        
        holder.tvFecha.text = context.getString(R.string.formato_fecha_hora, cita.fecha, cita.hora)
        
        // Texto del título con el estado
        val estadoTexto = when(cita.estado) {
            "ACEPTADO" -> "¡Paseador asignado! 🐕"
            "EN_CURSO" -> "En paseo ahora 🐾"
            "FINALIZADO" -> "Paseo completado ✅"
            else -> "Esperando paseador..."
        }
        
        holder.tvTitulo.text = "${cita.mascota} - $estadoTexto"
        
        val notasFinales = cita.notas.ifEmpty { context.getString(R.string.sin_notas) }
        val infoDetallada = context.getString(R.string.info_duracion, cita.duracion) + "\n" +
                           context.getString(R.string.info_precio, cita.precio) + "\n" +
                           context.getString(R.string.info_notas, notasFinales)
        
        holder.tvDetalles.text = infoDetallada

        // Imagen Inteligente
        val fotoKey = cita.foto ?: ""
        if (fotoKey.startsWith("avatar_")) {
            val idRes = context.resources.getIdentifier(fotoKey, "drawable", context.packageName)
            holder.ivMascota.setImageResource(if (idRes != 0) idRes else R.drawable.app_logo)
        } else if (fotoKey.isNotEmpty()) {
            val url = "https://manual-celibacy-tannery.ngrok-free.dev/dog_rider_api/uploads/$fotoKey"
            Glide.with(context).load(url).centerCrop().into(holder.ivMascota)
        }

        // El botón de eliminar solo funciona si el paseo aún no ha empezado (o según tu lógica)
        holder.btnEliminar.visibility = if (cita.estado == "PENDIENTE") View.VISIBLE else View.GONE
        holder.btnEliminar.setOnClickListener { onDeleteClick(cita) }
        
        // Manejar expansión
        holder.itemView.setOnClickListener {
            val estaVisible = holder.layoutDetalles.isVisible
            holder.layoutDetalles.isVisible = !estaVisible
            holder.ivExpand.setImageResource(
                if (estaVisible) android.R.drawable.arrow_down_float 
                else android.R.drawable.arrow_up_float,
            )
        }
    }

    override fun getItemCount(): Int = listaCitas.size
}
