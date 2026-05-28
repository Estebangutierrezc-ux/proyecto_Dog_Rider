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
    val telefono: String? = null
)

data class CitaRequest(
    val usuarioEmail: String,
    val mascota: String,
    val fecha: String,
    val hora: String,
    val duracion: String,
    val precio: String,
    val notas: String
)

data class UpdateProfileRequest(
    val email: String,
    val nombre: String,
    val apellido: String,
    val telefono: String
)
