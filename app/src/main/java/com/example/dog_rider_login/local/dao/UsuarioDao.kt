package com.example.dog_rider_login.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.dog_rider_login.local.entities.UsuarioLocal

@Dao
interface UsuarioDao {
    // Guardar un usuario en la base de datos del celular
    @Insert
    suspend fun insertarUsuario(usuario: UsuarioLocal)

    // Obtener todos los usuarios que aun no se han mandado a Oracle
    @Query("SELECT * FROM usuarios_pendientes WHERE sincronizado = 0")
    suspend fun obtenerUsuariosPendientes(): List<UsuarioLocal>

    // Marcar un usuario como ya enviado al servidor
    @Query("UPDATE usuarios_pendientes SET sincronizado = 1 WHERE id = :usuarioId")
    suspend fun marcarComoSincronizado(usuarioId: Int)
}
