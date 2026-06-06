package com.example.dog_rider_login.network

import com.example.dog_rider_login.network.models.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
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

    // Ruta para obtener todos los paseos pendientes (Para el Paseador)
    @GET("obtener_paseos_pendientes.php")
    fun obtenerPaseosPendientes(): Call<List<CitaRequest>>

    // Ruta para obtener todas las citas de un dueño (Para sincronizar estados)
    @GET("obtener_citas_dueno.php")
    fun obtenerCitasDueno(@retrofit2.http.Query("email") email: String): Call<List<CitaRequest>>

    // Ruta para aceptar un paseo
    @POST("aceptar_paseo.php")
    fun aceptarPaseo(@Body request: AceptarPaseoRequest): Call<AuthResponse>

    // Ruta para obtener el paseo que el paseador tiene activo
    @GET("obtener_paseo_activo.php")
    fun obtenerPaseoActivo(@retrofit2.http.Query("email") email: String): Call<CitaRequest>

    // Ruta para obtener el historial de paseos completados
    @GET("obtener_historial_paseador.php")
    fun obtenerHistorialPaseador(@retrofit2.http.Query("email") email: String): Call<List<CitaRequest>>

    // Ruta para finalizar un paseo
    @POST("finalizar_paseo.php")
    fun finalizarPaseo(@Body request: AceptarPaseoRequest): Call<AuthResponse>

    // Ruta para iniciar un paseo (Cambia a estado EN_CURSO)
    @POST("iniciar_paseo.php")
    fun iniciarPaseo(@Body request: AceptarPaseoRequest): Call<AuthResponse>

    // Ruta para eliminar un registro del historial (Paseador)
    @POST("eliminar_historial.php")
    fun eliminarHistorial(@Body request: AceptarPaseoRequest): Call<AuthResponse>

    // Ruta para eliminar una mascota (Dueño)
    @POST("eliminar_mascota.php")
    fun eliminarMascota(@Body request: Map<String, String>): Call<AuthResponse>

    // Ruta para eliminar una cita (Dueño)
    @POST("eliminar_cita_dueno.php")
    fun eliminarCitaDueno(@Body request: Map<String, Int>): Call<AuthResponse>

    // Ruta para actualizar el perfil del usuario
    @POST("actualizar_perfil.php")
    fun updateProfile(@Body request: UpdateProfileRequest): Call<AuthResponse>
}
