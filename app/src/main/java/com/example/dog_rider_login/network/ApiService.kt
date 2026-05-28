package com.example.dog_rider_login.network

import com.example.dog_rider_login.network.models.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// Interfaz que define todos los puntos de conexion (EndPoints) de mi API PHP
interface ApiService {
    
    // Ruta para iniciar sesion
    @POST("login.php")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    // Ruta para crear una cuenta nueva
    @POST("registrar.php")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    // Ruta para solicitar el codigo de recuperacion al correo
    @POST("recuperar_clave.php")
    fun recoverPassword(@Body request: RecoverRequest): Call<AuthResponse>

    // Ruta para validar el codigo y actualizar la clave en Oracle
    @POST("actualizar_clave.php")
    fun updatePassword(@Body request: ChangePasswordRequest): Call<AuthResponse>

    // Ruta para guardar un nuevo paseo en Oracle
    @POST("solicitar_paseo.php")
    fun solicitarPaseo(@Body request: CitaRequest): Call<AuthResponse>

    // Ruta para actualizar el perfil del usuario
    @POST("actualizar_perfil.php")
    fun updateProfile(@Body request: UpdateProfileRequest): Call<AuthResponse>
}
