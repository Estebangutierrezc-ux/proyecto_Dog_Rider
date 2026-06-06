package com.example.dog_rider_login.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dog_rider_login.local.dao.CitaDao
import com.example.dog_rider_login.local.dao.MascotaDao
import com.example.dog_rider_login.local.dao.UsuarioDao
import com.example.dog_rider_login.local.entities.CitaLocal
import com.example.dog_rider_login.local.entities.MascotaLocal
import com.example.dog_rider_login.local.entities.UsuarioLocal

// Clase que gestiona la base de datos interna del celular (Room)
@Database(entities = [UsuarioLocal::class, CitaLocal::class, MascotaLocal::class], version = 8, exportSchema = false)
abstract class BaseDatosLocal : RoomDatabase() {
    
    abstract fun usuarioDao(): UsuarioDao
    abstract fun citaDao(): CitaDao
    abstract fun mascotaDao(): MascotaDao

    companion object {
        @Volatile
        private var INSTANCE: BaseDatosLocal? = null

        fun obtenerInstancia(context: Context): BaseDatosLocal {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BaseDatosLocal::class.java,
                    "dog_rider_local_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
