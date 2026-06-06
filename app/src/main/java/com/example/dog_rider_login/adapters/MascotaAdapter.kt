package com.example.dog_rider_login.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dog_rider_login.R
import com.example.dog_rider_login.local.entities.MascotaLocal

class MascotaAdapter(
    private val listaMascotas: List<MascotaLocal>,
    private val onDeleteClick: (MascotaLocal) -> Unit
) : RecyclerView.Adapter<MascotaAdapter.MascotaViewHolder>() {

    class MascotaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivMascota: ImageView = view.findViewById(R.id.ivMascota)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreMascota)
        val tvRaza: TextView = view.findViewById(R.id.tvRazaMascota)
        val tvEdad: TextView = view.findViewById(R.id.tvEdadMascota)
        val tvDetalles: TextView = view.findViewById(R.id.tvDetallesExtra)
        val layoutDetalles: View = view.findViewById(R.id.layoutDetallesMascota)
        val ivExpand: ImageView = view.findViewById(R.id.ivExpandIcon)
        val btnEliminar: View = view.findViewById(R.id.btnEliminarMascota)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MascotaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mascota, parent, false)
        return MascotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MascotaViewHolder, position: Int) {
        val context = holder.itemView.context
        val mascota = listaMascotas[position]
        
        holder.tvNombre.text = mascota.nombre
        holder.tvRaza.text = mascota.raza
        holder.tvEdad.text = context.getString(R.string.formato_edad, mascota.edad)
        
        val detallesExtra = "Género: ${mascota.genero}\nNotas: ${mascota.notas.ifEmpty { context.getString(R.string.sin_notas) }}"
        holder.tvDetalles.text = detallesExtra

        // Cargar imagen
        val fotoKey = mascota.foto ?: ""
        if (fotoKey.startsWith("avatar_")) {
            val idRes = context.resources.getIdentifier(fotoKey, "drawable", context.packageName)
            holder.ivMascota.setImageResource(if (idRes != 0) idRes else R.drawable.app_logo)
        } else if (fotoKey.isNotEmpty()) {
            val urlImagen = "https://manual-celibacy-tannery.ngrok-free.dev/dog_rider_api/uploads/$fotoKey"
            Glide.with(context).load(urlImagen).placeholder(android.R.drawable.ic_menu_gallery).centerCrop().into(holder.ivMascota)
        }

        holder.btnEliminar.setOnClickListener { onDeleteClick(mascota) }
        
        holder.itemView.setOnClickListener {
            val estaVisible = holder.layoutDetalles.isVisible
            holder.layoutDetalles.isVisible = !estaVisible
            holder.ivExpand.setImageResource(
                if (estaVisible) android.R.drawable.arrow_down_float 
                else android.R.drawable.arrow_up_float
            )
        }
    }

    override fun getItemCount(): Int = listaMascotas.size
}
