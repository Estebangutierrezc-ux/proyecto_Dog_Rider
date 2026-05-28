package com.example.dog_rider_login.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.dog_rider_login.R
import com.example.dog_rider_login.local.entities.CitaLocal

class CitaAdapter(private val listaCitas: List<CitaLocal>) :
    RecyclerView.Adapter<CitaAdapter.CitaViewHolder>() {

    class CitaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFecha: TextView = view.findViewById(R.id.tvFechaCita)
        val tvTitulo: TextView = view.findViewById(R.id.tvTituloCita)
        val tvDetalles: TextView = view.findViewById(R.id.tvDetallesCita)
        val layoutDetalles: View = view.findViewById(R.id.layoutDetallesCita)
        val ivExpand: ImageView = view.findViewById(R.id.ivExpandIconCita)
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
        holder.tvTitulo.text = context.getString(R.string.titulo_paseo_mascota, cita.mascota)
        
        val notasFinales = cita.notas.ifEmpty { context.getString(R.string.sin_notas) }
        val infoDetallada = context.getString(R.string.info_duracion, cita.duracion) + "\n" +
                           context.getString(R.string.info_precio, cita.precio) + "\n" +
                           context.getString(R.string.info_notas, notasFinales)
        
        holder.tvDetalles.text = infoDetallada
        
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
