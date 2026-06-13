package com.example.dog_rider_login.network

import com.example.dog_rider_login.utils.Constants
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Objeto Singleton para gestionar la conexion global con el servidor PHP
object RetrofitClient {
    // URL global desde constantes
    private const val BASE_URL = Constants.BASE_URL

    // Configurar Gson para que sea flexible con las respuestas del PHP
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    // Configurar los tiempos de espera y headers para ngrok
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("ngrok-skip-browser-warning", "true")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    // Inicializar el cliente de Retrofit solo cuando se necesite (Lazy)
    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        retrofit.create(ApiService::class.java)
    }
}
