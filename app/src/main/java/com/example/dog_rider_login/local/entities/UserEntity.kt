package com.example.dog_rider_login.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// Esta clase representa la tabla local donde guardaremos los registros pendientes
@Entity(tableName = "pending_users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val email: String,
    val clave: String,
    val esPaseador: Boolean,
    val isSynced: Boolean = false // Para saber si ya se mando a Oracle
)
