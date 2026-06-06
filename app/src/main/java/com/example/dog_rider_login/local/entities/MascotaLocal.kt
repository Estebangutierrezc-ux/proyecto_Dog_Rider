package com.example.dog_rider_login.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mascotas")
data class MascotaLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val duenoEmail: String,
    val nombre: String,
    val raza: String,
    val edad: String,
    val genero: String,
    val notas: String,
    val foto: String? = null // Guardaremos el nombre del archivo o la ruta local
)
