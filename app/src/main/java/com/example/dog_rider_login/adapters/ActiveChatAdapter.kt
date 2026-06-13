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
import com.example.dog_rider_login.utils.Constants
import com.example.dog_rider_login.utils.SessionManager

class ActiveChatAdapter(
    private var items: List<CitaRequest>,
    private val onClick: (CitaRequest) -> Unit
) : RecyclerView.Adapter<ActiveChatAdapter.VH>() {

    fun updateItems(newItems: List<CitaRequest>) {
        items = newItems
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val ivMascota: ImageView = v.findViewById(R.id.ivChatMascota)
        val tvName: TextView = v.findViewById(R.id.tvChatUserName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_active_chat, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val context = holder.itemView.context
        val sessionManager = SessionManager(context)

        // IMPORTANTE: Mostrar NOMBRE, no correo
        val isWalker = sessionManager.isWalker()
        val displayName = if (isWalker) {
            // El paseador ve al dueño
            item.duenoNombre ?: context.getString(R.string.default_owner_name)
        } else {
            // El dueño ve al paseador (Aquí asumimos que la API nos da el nombre del paseador o usamos un formato)
            // Si la API solo da email, usamos un placeholder o buscamos el nombre. 
            // Según strings.xml tenemos format_walker_chat_name
            context.getString(R.string.format_walker_chat_name, item.mascota)
        }
        
        holder.tvName.text = displayName

        val foto = item.foto
        if (!foto.isNullOrEmpty()) {
            if (foto.startsWith("avatar_")) {
                val idRes = context.resources.getIdentifier(foto, "drawable", context.packageName)
                holder.ivMascota.setImageResource(if (idRes != 0) idRes else R.drawable.app_logo)
            } else {
                val url = Constants.UPLOADS_URL + foto
                Glide.with(context).load(url).centerCrop().into(holder.ivMascota)
            }
        } else {
            holder.ivMascota.setImageResource(R.drawable.app_logo)
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}
