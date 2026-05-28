package com.example.dog_rider_login.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// Esta clase define la tabla local para guardar usuarios cuando no hay internet
@Entity(tableName = "usuarios_pendientes")
data class UsuarioLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val email: String,
    val clave: String,
    val esPaseador: Boolean,
    val sincronizado: Boolean = false
)
