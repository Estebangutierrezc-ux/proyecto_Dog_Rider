package com.example.dog_rider_login.models

data class Notificacion(
    val id: Int,
    val titulo: String,
    val mensaje: String,
    val hora: String,
    val leida: Boolean = false,
    val tipo: String = "info" // "info", "mensaje", "alerta"
)
