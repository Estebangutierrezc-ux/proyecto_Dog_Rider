package com.example.dog_rider_login.local.entities

import androidx.room.Entity

// Definimos que el Nombre y el Email juntos son la clave unica para evitar duplicados
@Entity(tableName = "mascotas", primaryKeys = ["nombre", "duenoEmail"])
data class MascotaLocal(
    val duenoEmail: String,
    val nombre: String,
    val raza: String,
    val edad: String,
    val genero: String,
    val notas: String,
    val foto: String? = null
)
