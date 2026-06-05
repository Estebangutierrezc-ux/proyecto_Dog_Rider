package com.example.dog_rider_login.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.dog_rider_login.R
import com.example.dog_rider_login.network.models.CitaRequest

class HistorialAdapter(
    private val listaHistorial: List<CitaRequest>,
    private val onDeleteClick: (CitaRequest) -> Unit
) : RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder>() {

    class HistorialViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFecha: TextView = view.findViewById(R.id.tvFechaCita)
        val tvTitulo: TextView = view.findViewById(R.id.tvTituloCita)
        val tvDetalles: TextView = view.findViewById(R.id.tvDetallesCita)
        val layoutDetalles: View = view.findViewById(R.id.layoutDetallesCita)
        val ivExpand: ImageView = view.findViewById(R.id.ivExpandIconCita)
        val btnEliminar: Button = view.findViewById(R.id.btnEliminarHistorial)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cita, parent, false)
        return HistorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val context = holder.itemView.context
        val item = listaHistorial[position]

        holder.tvFecha.text = context.getString(R.string.formato_fecha_hora, item.fecha, item.hora)
        holder.tvTitulo.text = context.getString(R.string.titulo_paseo_mascota, item.mascota)

        val info = "Precio: ${item.precio}\nEstado: Completado ✅"
        holder.tvDetalles.text = info

        // El botón eliminar solo se ve al expandir
        holder.btnEliminar.visibility = View.VISIBLE 

        holder.itemView.setOnClickListener {
            val estaVisible = holder.layoutDetalles.isVisible
            holder.layoutDetalles.isVisible = !estaVisible
            holder.ivExpand.setImageResource(
                if (estaVisible) android.R.drawable.arrow_down_float 
                else android.R.drawable.arrow_up_float
            )
        }

        holder.btnEliminar.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount(): Int = listaHistorial.size
}
