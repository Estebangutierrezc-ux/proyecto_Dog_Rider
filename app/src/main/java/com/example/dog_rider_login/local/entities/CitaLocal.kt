package com.example.dog_rider_login.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "citas")
data class CitaLocal(
    @PrimaryKey val id: Int, // Usaremos el ID real de Oracle para sincronizar
    val usuarioEmail: String,
    val mascota: String,
    val fecha: String,
    val hora: String,
    val duracion: String,
    val precio: String,
    val notas: String,
    val foto: String? = null,
    val estado: String = "PENDIENTE", // Nuevo: Para saber si está aceptado, en curso o finalizado
    val timestamp: Long = System.currentTimeMillis()
)
