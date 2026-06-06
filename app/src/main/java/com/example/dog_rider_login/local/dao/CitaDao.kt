package com.example.dog_rider_login.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dog_rider_login.local.entities.CitaLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface CitaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCita(cita: CitaLocal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCitas(citas: List<CitaLocal>)

    @Query("SELECT * FROM citas WHERE usuarioEmail = :email ORDER BY id DESC")
    fun obtenerCitasPorUsuario(email: String): Flow<List<CitaLocal>>

    @Query("DELETE FROM citas WHERE id = :id")
    suspend fun eliminarCitaPorId(id: Int)

    @Query("SELECT COUNT(*) FROM citas WHERE mascota = :mascota AND fecha = :fecha AND hora = :hora AND usuarioEmail = :email")
    suspend fun verificarDuplicado(mascota: String, fecha: String, hora: String, email: String): Int

    @Query("DELETE FROM citas")
    suspend fun eliminarTodasLasCitas()
}
