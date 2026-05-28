package com.example.dog_rider_login.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.dog_rider_login.local.BaseDatosLocal
import com.example.dog_rider_login.network.RetrofitClient
import com.example.dog_rider_login.network.models.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Este trabajador se encarga de mandar los datos a Oracle en segundo plano
class SincronizacionWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val db = BaseDatosLocal.obtenerInstancia(applicationContext)
        val usuariosPendientes = db.usuarioDao().obtenerUsuariosPendientes()

        if (usuariosPendientes.isEmpty()) return@withContext Result.success()

        var huboFallo = false

        for (usuario in usuariosPendientes) {
            try {
                val request = RegisterRequest(
                    usuario.nombre,
                    usuario.apellido,
                    usuario.telefono,
                    usuario.email,
                    usuario.clave, // Ojo: aqui se manda la clave que ya estaba en el objeto
                    usuario.esPaseador
                )

                // Llamada síncrona (execute) ya que estamos en un hilo de fondo
                val response = RetrofitClient.instance.register(request).execute()

                if (response.isSuccessful && response.body()?.success == true) {
                    db.usuarioDao().marcarComoSincronizado(usuario.id)
                } else {
                    huboFallo = true
                }
            } catch (e: Exception) {
                huboFallo = true
            }
        }

        if (huboFallo) Result.retry() else Result.success()
    }
}
