package com.example.dog_rider_login.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dog_rider_login.R
import com.example.dog_rider_login.network.models.ChatMessage

class ChatAdapter(
    private val myEmail: String,
    private val messages: MutableList<ChatMessage>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ME = 1
    private val VIEW_TYPE_OTHER = 2

    override fun getItemViewType(position: Int): Int {
        val emisor = messages[position].emisor.trim()
        return if (emisor.equals(myEmail.trim(), ignoreCase = true)) VIEW_TYPE_ME else VIEW_TYPE_OTHER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ME) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_me, parent, false)
            MeViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_other, parent, false)
            OtherViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        val timeText = formatTime(msg.fecha)

        if (holder is MeViewHolder) {
            holder.tvMsg.text = msg.texto
            holder.tvTime.text = timeText
            // Lógica de checks
            holder.tvReadStatus.text = if (msg.leido) "✓✓" else "✓"
            holder.tvReadStatus.setTextColor(if (msg.leido) android.graphics.Color.CYAN else android.graphics.Color.LTGRAY)
        } else if (holder is OtherViewHolder) {
            holder.tvMsg.text = msg.texto
            holder.tvTime.text = timeText
        }
    }

    private fun formatTime(fecha: String?): String {
        if (fecha.isNullOrEmpty()) return ""
        return try {
            val parts = fecha.split(" ")
            if (parts.size > 1) parts[1].substring(0, 5) else ""
        } catch (e: Exception) { "" }
    }

    override fun getItemCount() = messages.size

    fun updateMessages(newMessages: List<ChatMessage>) {
        // Forzamos la actualización siempre que haya mensajes nuevos
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    class MeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMsg: TextView = view.findViewById(R.id.tvMessageMe)
        val tvTime: TextView = view.findViewById(R.id.tvTimeMe)
        val tvReadStatus: TextView = view.findViewById(R.id.tvReadStatus)
    }

    class OtherViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMsg: TextView = view.findViewById(R.id.tvMessageOther)
        val tvTime: TextView = view.findViewById(R.id.tvTimeOther)
    }
}
