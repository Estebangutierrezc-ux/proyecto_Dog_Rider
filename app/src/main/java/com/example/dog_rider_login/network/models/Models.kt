package com.example.dog_rider_login.network.models

data class LoginRequest(
    val email: String,
    val clave: String
)

data class RegisterRequest(
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val email: String,
    val clave: String,
    val esPaseador: Boolean
)

data class RecoverRequest(
    val email: String
)

data class ChangePasswordRequest(
    val email: String,
    val codigo: String,
    val nuevaClave: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val nombre: String? = null,
    val apellido: String? = null,
    val telefono: String? = null,
    val esPaseador: Boolean? = null,
    val citaId: Int? = null // Añadimos para recibir el ID al crear cita
)

data class CitaRequest(
    val id: Int? = null,
    val usuarioEmail: String? = null, 
    val duenoNombre: String? = null,
    val mascota: String,
    val raza: String? = null,  // Añadimos Raza real
    val edad: String? = null,  // Añadimos Edad real
    val fecha: String,
    val hora: String,
    val duracion: String,
    val precio: String,
    val notas: String,
    val foto: String? = null, // Añadimos campo foto
    val estado: String? = "PENDIENTE"
)

data class AceptarPaseoRequest(
    val citaId: Int,
    val paseadorEmail: String
)

data class UpdateProfileRequest(
    val email: String,
    val nombre: String,
    val apellido: String,
    val telefono: String
)
