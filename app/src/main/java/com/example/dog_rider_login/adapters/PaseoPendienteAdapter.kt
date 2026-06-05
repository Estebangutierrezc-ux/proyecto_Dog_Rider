package com.example.dog_rider_login.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dog_rider_login.R
import com.example.dog_rider_login.network.models.CitaRequest

class PaseoPendienteAdapter(
    private val listaPaseos: List<CitaRequest>,
    private val onAceptarClick: (CitaRequest) -> Unit
) : RecyclerView.Adapter<PaseoPendienteAdapter.PaseoViewHolder>() {

    class PaseoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombrePerro)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecioPaseo)
        val tvInfo: TextView = view.findViewById(R.id.tvInfoPaseo)
        val btnAceptar: View = view.findViewById(R.id.btnAceptarPaseo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaseoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_paseo_disponible, parent, false)
        return PaseoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaseoViewHolder, position: Int) {
        val context = holder.itemView.context
        val paseo = listaPaseos[position]

        holder.tvNombre.text = paseo.mascota
        holder.tvPrecio.text = paseo.precio
        
        // Formatear info: Fecha, Hora y Duración
        val infoCompleta = "${context.getString(R.string.formato_fecha_hora, paseo.fecha, paseo.hora)} (${paseo.duracion})"
        holder.tvInfo.text = infoCompleta

        holder.btnAceptar.setOnClickListener {
            onAceptarClick(paseo)
        }
    }

    override fun getItemCount(): Int = listaPaseos.size
}
