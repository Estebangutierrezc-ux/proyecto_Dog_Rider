package com.example.dog_rider_login.models

data class Cita(
    val id: Int,
    val fecha: String, // Ejemplo: "Hoy, 3 PM"
    val titulo: String, // Ejemplo: "Paseo con Juan"
    val colorHex: String = "#CCCCCC" // Color de fondo de la tarjeta
)
