package com.example.dog_rider_login.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dog_rider_login.local.entities.MascotaLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface MascotaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMascota(mascota: MascotaLocal)

    @Query("SELECT * FROM mascotas WHERE duenoEmail = :email ORDER BY nombre ASC")
    fun obtenerMascotasPorDuenio(email: String): Flow<List<MascotaLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMascotas(mascotas: List<MascotaLocal>)

    @Query("DELETE FROM mascotas WHERE nombre = :nombre AND duenoEmail = :email")
    suspend fun eliminarMascotaPorNombre(nombre: String, email: String)

    @Query("DELETE FROM mascotas")
    suspend fun eliminarTodasLasMascotas()
}
