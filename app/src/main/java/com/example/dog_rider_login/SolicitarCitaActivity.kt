package com.example.dog_rider_login

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dog_rider_login.local.BaseDatosLocal
import com.example.dog_rider_login.local.entities.CitaLocal
import com.example.dog_rider_login.local.entities.MascotaLocal
import com.example.dog_rider_login.network.RetrofitClient
import com.example.dog_rider_login.network.models.AuthResponse
import com.example.dog_rider_login.network.models.CitaRequest
import com.example.dog_rider_login.utils.NavigationUtils
import com.example.dog_rider_login.utils.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Locale

class SolicitarCitaActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private var listaMascotasCargada = listOf<MascotaLocal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitar_cita)

        sessionManager = SessionManager(this)

        val btnBack = findViewById<ImageButton>(R.id.btnBackSolicitarCita)
        val spinnerMascotas = findViewById<Spinner>(R.id.spinnerMascotasCita)
        val tvFecha = findViewById<TextView>(R.id.tvFechaSeleccionada)
        val tvHora = findViewById<TextView>(R.id.tvHoraSeleccionada)
        val tvPrecioEstimado = findViewById<TextView>(R.id.tvPrecioEstimado)
        val chipGroupDuracion = findViewById<com.google.android.material.chip.ChipGroup>(R.id.chipGroupDuracion)
        val etDuracionPersonalizada = findViewById<android.widget.EditText>(R.id.etDuracionPersonalizada)
        val etNotas = findViewById<android.widget.EditText>(R.id.etNotasCita)
        val tvCharCounter = findViewById<TextView>(R.id.tvCharCounter)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmarCita)

        btnBack.setOnClickListener { finish() }

        // Navigation Bar Inferior
        NavigationUtils.configurarNavegacion(this)

        // Contador de caracteres para notas
        etNotas.addTextChangedListener(
            object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val currentLength = s?.length ?: 0
                    tvCharCounter.text = getString(R.string.counter_150, currentLength)
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            },
        )

        // Cargar Mascotas Reales del Usuario en el Spinner
        val emailUser = sessionManager.getUserEmail() ?: ""

        lifecycleScope.launch {
            BaseDatosLocal.obtenerInstancia(this@SolicitarCitaActivity)
                .mascotaDao()
                .obtenerMascotasPorDuenio(emailUser)
                .collectLatest { listaMascotas ->
                    listaMascotasCargada = listaMascotas
                    val nombres = mutableListOf(getString(R.string.select_option))
                    nombres.addAll(listaMascotas.map { it.nombre })
                    
                    val adapterMascotas = ArrayAdapter(this@SolicitarCitaActivity, R.layout.item_spinner, nombres)
                    adapterMascotas.setDropDownViewResource(R.layout.item_spinner)
                    spinnerMascotas.adapter = adapterMascotas
                }
        }

        // Lógica de Precios y Duración (en CLP)
        val precioPorMinuto = 167 // Aprox $10.000 por hora
        val formatoCLP = java.text.DecimalFormat("$#,###")
        
        fun actualizarPrecio(minutos: Int) {
            val total = minutos * precioPorMinuto
            tvPrecioEstimado.text = formatoCLP.format(total)
        }

        chipGroupDuracion.setOnCheckedStateChangeListener { _, checkedIds ->
            etDuracionPersonalizada.visibility = View.GONE
            val checkedId = checkedIds.firstOrNull() ?: View.NO_ID
            when (checkedId) {
                R.id.chip30min -> actualizarPrecio(30)
                R.id.chip60min -> actualizarPrecio(60)
                R.id.chip90min -> actualizarPrecio(90)
                R.id.chipCustom -> {
                    etDuracionPersonalizada.visibility = View.VISIBLE
                    tvPrecioEstimado.text = getString(R.string.precio_cero)
                }
            }
        }

        etDuracionPersonalizada.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val mins = s.toString().toIntOrNull() ?: 0
                actualizarPrecio(mins)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Configurar DatePicker
        tvFecha.setOnClickListener {
            val c = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    tvFecha.text = getString(R.string.formato_fecha, day, month + 1, year)
                },
                c[Calendar.YEAR],
                c[Calendar.MONTH],
                c[Calendar.DAY_OF_MONTH],
            )
            
            // No permitir seleccionar días anteriores al actual
            datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.show()
        }

        // Configurar TimePicker
        tvHora.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                // Validar horario laboral: 07:30 a 23:00
                val horaEnMinutos = (hour * 60) + minute
                val minLaboral = (7 * 60) + 30 // 07:30
                val maxLaboral = 23 * 60     // 23:00

                if (horaEnMinutos in minLaboral..maxLaboral) {
                    tvHora.text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                } else {
                    Toast.makeText(this, getString(R.string.error_laboral), Toast.LENGTH_LONG).show()
                }
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        btnConfirmar.setOnClickListener {
            val mascota = spinnerMascotas.selectedItem.toString()
            val chipId = chipGroupDuracion.checkedChipIds.firstOrNull() ?: View.NO_ID
            
            val duracionValida = when (chipId) {
                R.id.chip30min, R.id.chip60min, R.id.chip90min -> true
                R.id.chipCustom -> etDuracionPersonalizada.text.isNotEmpty()
                else -> false
            }

            if (mascota == "Seleccionar" || !duracionValida) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fecha = tvFecha.text.toString()
            val hora = tvHora.text.toString()

            if (fecha == "Seleccionar..." || hora == "--:--") {
                Toast.makeText(this, "Por favor seleccione fecha y hora", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val precio = tvPrecioEstimado.text.toString()
            val notas = etNotas.text.toString()

            // Obtener el email del usuario logueado de forma segura
            val emailLogueado = sessionManager.getUserEmail() ?: ""

            // Buscar la foto de la mascota seleccionada
            val mascotaSeleccionada = listaMascotasCargada.find { it.nombre == mascota }
            val fotoMascota = mascotaSeleccionada?.foto

            // Verificar duplicados antes de procesar
            lifecycleScope.launch {
                val db = BaseDatosLocal.obtenerInstancia(this@SolicitarCitaActivity)
                val duplicados = db.citaDao().verificarDuplicado(mascota, fecha, hora, emailLogueado)

                if (duplicados > 0) {
                    Toast.makeText(this@SolicitarCitaActivity, 
                        getString(R.string.error_duplicado, mascota), 
                        Toast.LENGTH_LONG).show()
                } else {
                    val duracion = when (chipId) {
                        R.id.chip30min -> "30 min"
                        R.id.chip60min -> "60 min"
                        R.id.chip90min -> "90 min"
                        else -> "${etDuracionPersonalizada.text} min"
                    }

                    // 1. Intentar guardar en Oracle (Cloud)
                    val request = CitaRequest(
                        usuarioEmail = emailLogueado,
                        mascota = mascota,
                        fecha = fecha,
                        hora = hora,
                        duracion = duracion,
                        precio = precio,
                        notas = notas,
                        foto = fotoMascota // Enviamos la foto a Oracle
                    )
                    Toast.makeText(this@SolicitarCitaActivity, getString(R.string.msg_sincronizando), Toast.LENGTH_SHORT).show()

                    RetrofitClient.instance.solicitarPaseo(request).enqueue(object : Callback<AuthResponse> {
                        override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                            val resBody = response.body()
                            if (response.isSuccessful && resBody?.success == true) {
                                // 2. Si se guardó en Oracle, guardamos en la base de datos local (Room)
                                lifecycleScope.launch {
                                    val nuevaCita = CitaLocal(
                                        id = resBody.citaId ?: 0,
                                        usuarioEmail = emailLogueado,
                                        mascota = mascota,
                                        fecha = fecha,
                                        hora = hora,
                                        duracion = duracion,
                                        precio = precio,
                                        notas = notas,
                                        foto = fotoMascota
                                    )
                                    
                                    db.citaDao().insertarCita(nuevaCita)

                                    Toast.makeText(this@SolicitarCitaActivity, "Paseo guardado con éxito", Toast.LENGTH_LONG).show()
                                    finish()
                                }
                            } else {
                                val msg = response.body()?.message ?: "Error al guardar en el servidor"
                                Toast.makeText(this@SolicitarCitaActivity, msg, Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                            Toast.makeText(this@SolicitarCitaActivity, "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
                }
            }
        }
    }
}
