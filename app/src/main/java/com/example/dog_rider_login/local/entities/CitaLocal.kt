package com.example.dog_rider_login.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "citas")
data class CitaLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioEmail: String, // Nueva columna para separar por perfil
    val mascota: String,
    val fecha: String,
    val hora: String,
    val duracion: String,
    val precio: String,
    val notas: String,
    val timestamp: Long = System.currentTimeMillis()
)
