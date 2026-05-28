package com.example.dog_rider_login.models

data class Mascota(
    val id: Int,
    val nombre: String,
    val raza: String,
    val edad: String,
    val imagenResId: Int? = null // Para usar una imagen por defecto o recursos locales por ahora
)
