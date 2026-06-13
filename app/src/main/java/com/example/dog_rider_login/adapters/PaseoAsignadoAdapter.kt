package com.example.dog_rider_login.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dog_rider_login.R
import com.example.dog_rider_login.network.models.CitaRequest

class PaseoAsignadoAdapter(
    private val list: List<CitaRequest>,
    private val onClick: (CitaRequest) -> Unit
) : RecyclerView.Adapter<PaseoAsignadoAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.ivDogAsignado)
        val name: TextView = v.findViewById(R.id.tvDogNameAsignado)
        val info: TextView = v.findViewById(R.id.tvDogInfoAsignado)
        val status: TextView = v.findViewById(R.id.tvStatusAsignado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_paseo_asignado, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = list[position]
        val context = holder.itemView.context
        
        holder.name.text = p.mascota
        holder.info.text = "${p.raza} • ${p.hora}"
        holder.status.text = p.estado
        
        if (p.estado == "EN_CURSO") {
            holder.status.setTextColor(android.graphics.Color.parseColor("#4CAF50")) // Verde
        }

        val foto = p.foto ?: ""
        if (foto.startsWith("avatar_")) {
            val id = context.resources.getIdentifier(foto, "drawable", context.packageName)
            holder.img.setImageResource(if (id != 0) id else R.drawable.app_logo)
        } else if (foto.isNotEmpty()) {
            val url = "https://manual-celibacy-tannery.ngrok-free.dev/dog_rider_api/uploads/$foto"
            Glide.with(context).load(url).centerCrop().into(holder.img)
        } else {
            holder.img.setImageResource(R.drawable.app_logo)
        }

        holder.itemView.setOnClickListener { onClick(p) }
    }

    override fun getItemCount() = list.size
}
