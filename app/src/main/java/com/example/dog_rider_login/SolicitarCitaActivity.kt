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
        NavigationUtils.configurarNavegacion(this)

        setupUI()
        cargarMascotas()
    }

    private fun setupUI() {
        val btnBack = findViewById<ImageButton>(R.id.btnBackSolicitarCita)
        val tvFecha = findViewById<TextView>(R.id.tvFechaSeleccionada)
        val tvHora = findViewById<TextView>(R.id.tvHoraSeleccionada)
        val etNotas = findViewById<android.widget.EditText>(R.id.etNotasCita)
        val tvCharCounter = findViewById<TextView>(R.id.tvCharCounter)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmarCita)
        val chipGroupDuracion = findViewById<com.google.android.material.chip.ChipGroup>(R.id.chipGroupDuracion)
        val etDuracionPersonalizada = findViewById<android.widget.EditText>(R.id.etDuracionPersonalizada)
        val tvPrecioEstimado = findViewById<TextView>(R.id.tvPrecioEstimado)

        btnBack?.setOnClickListener { finish() }

        etNotas?.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvCharCounter?.text = getString(R.string.counter_150, s?.length ?: 0)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        val precioPorMinuto = 167
        val formatoCLP = java.text.DecimalFormat("$#,###")
        
        fun actualizarPrecio(minutos: Int) {
            val total = minutos * precioPorMinuto
            tvPrecioEstimado?.text = formatoCLP.format(total)
        }

        chipGroupDuracion?.setOnCheckedStateChangeListener { _, checkedIds ->
            etDuracionPersonalizada?.visibility = View.GONE
            val checkedId = checkedIds.firstOrNull() ?: View.NO_ID
            when (checkedId) {
                R.id.chip30min -> actualizarPrecio(30)
                R.id.chip60min -> actualizarPrecio(60)
                R.id.chip90min -> actualizarPrecio(90)
                R.id.chipCustom -> {
                    etDuracionPersonalizada?.visibility = View.VISIBLE
                    tvPrecioEstimado?.text = getString(R.string.precio_cero)
                }
            }
        }

        etDuracionPersonalizada?.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val mins = s.toString().toIntOrNull() ?: 0
                actualizarPrecio(mins)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        tvFecha?.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                tvFecha.text = getString(R.string.formato_fecha, day, month + 1, year)
            }, c[Calendar.YEAR], c[Calendar.MONTH], c[Calendar.DAY_OF_MONTH]).apply {
                datePicker.minDate = System.currentTimeMillis() - 1000
                show()
            }
        }

        tvHora?.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                val horaEnMinutos = (hour * 60) + minute
                if (horaEnMinutos in (7 * 60 + 30)..(23 * 60)) {
                    tvHora.text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                } else {
                    Toast.makeText(this, getString(R.string.error_laboral), Toast.LENGTH_LONG).show()
                }
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        btnConfirmar?.setOnClickListener { validarYEnviar() }
    }

    private fun cargarMascotas() {
        val spinnerMascotas = findViewById<Spinner>(R.id.spinnerMascotasCita) ?: return
        val emailUser = sessionManager.getUserEmail() ?: ""

        lifecycleScope.launch {
            BaseDatosLocal.obtenerInstancia(this@SolicitarCitaActivity)
                .mascotaDao()
                .obtenerMascotasPorDuenio(emailUser)
                .collectLatest { listaMascotas ->
                    listaMascotasCargada = listaMascotas
                    val nombres = mutableListOf(getString(R.string.select_option))
                    nombres.addAll(listaMascotas.map { it.nombre })
                    
                    val adapter = ArrayAdapter(this@SolicitarCitaActivity, R.layout.item_spinner, nombres)
                    adapter.setDropDownViewResource(R.layout.item_spinner)
                    spinnerMascotas.adapter = adapter
                }
        }
    }

    private fun validarYEnviar() {
        val spinnerMascotas = findViewById<Spinner>(R.id.spinnerMascotasCita)
        val chipGroupDuracion = findViewById<com.google.android.material.chip.ChipGroup>(R.id.chipGroupDuracion)
        val etDuracionPersonalizada = findViewById<android.widget.EditText>(R.id.etDuracionPersonalizada)
        val tvFecha = findViewById<TextView>(R.id.tvFechaSeleccionada)
        val tvHora = findViewById<TextView>(R.id.tvHoraSeleccionada)
        val tvPrecioEstimado = findViewById<TextView>(R.id.tvPrecioEstimado)
        val etNotas = findViewById<android.widget.EditText>(R.id.etNotasCita)

        val mascota = spinnerMascotas?.selectedItem.toString()
        val chipId = chipGroupDuracion?.checkedChipIds?.firstOrNull() ?: View.NO_ID
        
        val duracionValida = when (chipId) {
            R.id.chip30min, R.id.chip60min, R.id.chip90min -> true
            R.id.chipCustom -> etDuracionPersonalizada?.text?.isNotEmpty() ?: false
            else -> false
        }

        if (mascota == getString(R.string.select_option) || !duracionValida) {
            Toast.makeText(this, getString(R.string.error_campos_vacios), Toast.LENGTH_SHORT).show()
            return
        }

        val fecha = tvFecha?.text.toString()
        val hora = tvHora?.text.toString()

        if (fecha == getString(R.string.seleccionar_punto) || hora == getString(R.string.hora_vacia)) {
            Toast.makeText(this, getString(R.string.error_seleccionar_fecha_hora), Toast.LENGTH_SHORT).show()
            return
        }

        val emailLogueado = sessionManager.getUserEmail() ?: ""
        val mascotaSeleccionada = listaMascotasCargada.find { it.nombre == mascota }
        val duracion = when (chipId) {
            R.id.chip30min -> "30 min"
            R.id.chip60min -> "60 min"
            R.id.chip90min -> "90 min"
            else -> "${etDuracionPersonalizada?.text} min"
        }

        lifecycleScope.launch {
            val db = BaseDatosLocal.obtenerInstancia(this@SolicitarCitaActivity)
            if (db.citaDao().verificarDuplicado(mascota, fecha, hora, emailLogueado) > 0) {
                Toast.makeText(this@SolicitarCitaActivity, getString(R.string.error_duplicado, mascota), Toast.LENGTH_LONG).show()
            } else {
                enviarCitaAlServidor(emailLogueado, mascota, fecha, hora, duracion, tvPrecioEstimado?.text.toString(), etNotas?.text.toString(), mascotaSeleccionada?.foto)
            }
        }
    }

    private fun enviarCitaAlServidor(email: String, mascota: String, fecha: String, hora: String, duracion: String, precio: String, notas: String, foto: String?) {
        val request = CitaRequest(null, email, null, mascota, null, null, fecha, hora, duracion, precio, notas, foto, "PENDIENTE", null)
        Toast.makeText(this, getString(R.string.msg_sincronizando), Toast.LENGTH_SHORT).show()

        RetrofitClient.instance.solicitarPaseo(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                val resBody = response.body()
                if (response.isSuccessful && resBody?.success == true) {
                    lifecycleScope.launch {
                        val nuevaCita = CitaLocal(resBody.citaId ?: 0, email, mascota, fecha, hora, duracion, precio, notas, foto, "PENDIENTE", null)
                        BaseDatosLocal.obtenerInstancia(this@SolicitarCitaActivity).citaDao().insertarCita(nuevaCita)
                        Toast.makeText(this@SolicitarCitaActivity, getString(R.string.msg_paseo_guardado_exito), Toast.LENGTH_LONG).show()
                        finish()
                    }
                } else {
                    val msg = resBody?.message ?: getString(R.string.error_servidor_guardado)
                    Toast.makeText(this@SolicitarCitaActivity, msg, Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(this@SolicitarCitaActivity, getString(R.string.error_conexion_servidor), Toast.LENGTH_LONG).show()
            }
        })
    }
}
