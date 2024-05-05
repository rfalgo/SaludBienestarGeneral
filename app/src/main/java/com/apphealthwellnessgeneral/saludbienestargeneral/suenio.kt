package com.apphealthwellnessgeneral.saludbienestargeneral

import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class suenio : AppCompatActivity() {

    private var currentUser: FirebaseUser? = null

    private val suenioList = mutableListOf<SleepData>()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userId: String
    private var childEventListener: ChildEventListener? = null
    private val calidadEntries = mutableListOf<Entry>()

    private lateinit var spinnerCalidadSuenio: Spinner
    private lateinit var calidadSuenioArray: Array<String>
    private lateinit var calidadSuenioAdapter: ArrayAdapter<String>
    private val duracionEntries = mutableListOf<BarEntry>()
    private lateinit var textViewSuenioRecords: TextView
    private lateinit var combinedChart: CombinedChart

    private lateinit var editTextHoraInicio: EditText
    private lateinit var editTextHoraFin: EditText

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suenio)

        currentUser = FirebaseAuth.getInstance().currentUser
        databaseReference = FirebaseDatabase.getInstance().reference

        combinedChart = findViewById(R.id.combinedChart)
        combinedChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        editTextHoraInicio = findViewById(R.id.editTextHoraInicio)
        editTextHoraFin = findViewById(R.id.editTextHoraFin)
        textViewSuenioRecords = findViewById(R.id.textViewSuenioRecords)
        spinnerCalidadSuenio = findViewById(R.id.spinnerCalidadSuenio)

        calidadSuenioArray = resources.getStringArray(R.array.calidad_suenio)
        calidadSuenioAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, calidadSuenioArray)
        calidadSuenioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCalidadSuenio.adapter = calidadSuenioAdapter

        editTextHoraInicio.setOnClickListener {
            mostrarSelectorHora(editTextHoraInicio)
        }

        editTextHoraFin.setOnClickListener {
            mostrarSelectorHora(editTextHoraFin)
        }

        userId = currentUser?.uid ?: ""

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // Lógica cuando se añade un niño
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Lógica cuando un niño ha cambiado
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Lógica cuando un niño ha sido eliminado
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Lógica cuando un niño ha cambiado de posición
            }

            override fun onCancelled(error: DatabaseError) {
                // Lógica cuando la operación es cancelada
            }
        }

        val btnGuardarDatos: Button = findViewById(R.id.btnGuardarDatos)
        btnGuardarDatos.setOnClickListener {
            saveSuenioData() // Llama al método saveSuenioData() cuando se presione el botón
        }

        loadSuenioDataFromSharedPreferences()
        updateChart()
    }

    private fun parseTimeToMillis(time: String): Long {
        val parts = time.split(":")
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hours)
        calendar.set(Calendar.MINUTE, minutes)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun calcularDuracionSuenio(startTime: Long, endTime: Long): String {
        val diferencia = endTime - startTime
        val duracionMinutos: Long

        if (diferencia < 0) {
            val finDiaSiguiente = endTime + 24 * 60 * 60 * 1000
            val nuevaDiferencia = finDiaSiguiente - startTime
            duracionMinutos = nuevaDiferencia / (1000 * 60)
        } else {
            duracionMinutos = diferencia / (1000 * 60)
        }

        val horas = duracionMinutos / 60
        val minutos = duracionMinutos % 60

        return String.format("%02d:%02d", horas, minutos)
    }

    private fun mostrarSelectorHora(editText: EditText) {
        val calendar = Calendar.getInstance()
        val hora = calendar.get(Calendar.HOUR_OF_DAY)
        val minutos = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val horaSeleccionada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                editText.setText(horaSeleccionada)
            },
            hora,
            minutos,
            true
        )
        timePickerDialog.show()
    }

    private fun saveSuenioData() {
        try {
            val startTimeMillis = parseTimeToMillis(editTextHoraInicio.text.toString())
            val endTimeMillis = parseTimeToMillis(editTextHoraFin.text.toString())
            val duracionSuenio = calcularDuracionSuenio(startTimeMillis, endTimeMillis)
            val calidadSuenio = spinnerCalidadSuenio.selectedItem.toString()
            val timestamp = System.currentTimeMillis() // Obtener la marca de tiempo actual
            val userEmail = currentUser?.email ?: "" // Obtener el correo electrónico del usuario actual

            // Convertir startTimeMillis y endTimeMillis a formato legible
            val startTime = convertirMillisAFecha(startTimeMillis, startTimeMillis, endTimeMillis).first
            val endTime = convertirMillisAFecha(endTimeMillis, startTimeMillis, endTimeMillis).second

            val suenioEntryReference = databaseReference.child("registroSuenio").child(userId).push()
            suenioEntryReference.child("startTime").setValue(startTime)
            suenioEntryReference.child("endTime").setValue(endTime)
            suenioEntryReference.child("duracion").setValue(duracionSuenio)
            suenioEntryReference.child("calidadSuenio").setValue(calidadSuenio)
            suenioEntryReference.child("userEmail").setValue(userEmail) // Guardar el correo electrónico del usuario

            val sleepData = SleepData(startTimeMillis, endTimeMillis, duracionSuenio, calidadSuenio, timestamp)
            suenioList.add(sleepData)

            updateChart()
            showSuenioRecords()

            saveSuenioDataToSharedPreferences() // Guardar datos en SharedPreferences

        } catch (e: Exception) {
            Toast.makeText(this, "Error al guardar datos de sueño", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    private fun saveSuenioDataToSharedPreferences() {
        val gson = Gson()
        val json = gson.toJson(suenioList)
        sharedPreferences.edit().putString(userId, json).apply() // Almacenar datos usando el UID como clave
    }


    private fun updateChart() {
        // Limpiar las listas de entradas
        duracionEntries.clear()
        calidadEntries.clear()

        // Agregar los datos a las listas
        for ((index, suenioEntry) in suenioList.withIndex()) {
            val duracionParts = suenioEntry.duracion.split(":")
            val horas: Int
            val minutos: Int
            if (duracionParts.size == 2) {
                horas = duracionParts[0].toInt()
                minutos = duracionParts[1].toInt()
            } else {
                horas = 0
                minutos = 0
            }
            val duracionEnHoras = horas + minutos.toFloat() / 60 // Convertir minutos a horas

            // Agregar entrada de duración
            duracionEntries.add(BarEntry(index.toFloat(), duracionEnHoras))

            // Calcular la calidad del sueño y agregar entrada de calidad
            val calidadFloat = convertirCalidadSuenio(suenioEntry.calidadSuenio)
            calidadEntries.add(Entry(index.toFloat(), calidadFloat))
        }

        // Configurar los conjuntos de datos
        val duracionSet = BarDataSet(duracionEntries, "Duración del sueño (horas)")
        duracionSet.color = Color.GREEN
        duracionSet.valueTextColor = Color.GREEN
        duracionSet.valueTextSize = 12f // Tamaño de la fuente para los números de duración

        val calidadSet = LineDataSet(calidadEntries, "Calidad del sueño")
        calidadSet.color = Color.RED
        calidadSet.valueTextColor = Color.RED
        calidadSet.valueTextSize = 12f // Tamaño de la fuente para los números de calidad

        // Crear el objeto CombinedData y configurar los datos
        val data = CombinedData()
        data.setData(LineData(calidadSet))
        data.setData(BarData(duracionSet))

        // Configurar el CombinedChart
        combinedChart.data = data

        // Ajustar el tamaño del gráfico según sea necesario
        combinedChart.setMinimumHeight(775)

        // Para deshabilitar las etiquetas de descripción (description)
        combinedChart.description.isEnabled = false

        combinedChart.xAxis.isEnabled = false // Deshabilitar el eje X

        // Invalidar el gráfico para que se actualice
        combinedChart.invalidate()
    }

    private fun convertirCalidadSuenio(calidadSuenio: String): Float {
        return calidadSuenio.toFloatOrNull() ?: 0f // Convertir el valor numérico a flotante
    }

    private fun convertirMillisAFecha(millis: Long, horaInicio: Long, horaFin: Long): Pair<String, String> {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        val horaInicioCalendar = Calendar.getInstance().apply {
            timeInMillis = horaInicio
        }
        val horaFinCalendar = Calendar.getInstance().apply {
            timeInMillis = horaFin
        }

        // Verificar si la hora de fin es anterior a la hora de inicio
        if (horaFinCalendar.before(horaInicioCalendar)) {
            // Añadir un día a la fecha de fin
            horaFinCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Formatear la hora de inicio y la hora de fin
        val horaInicioString = formato.format(horaInicioCalendar.time)
        val horaFinString = formato.format(horaFinCalendar.time)

        return horaInicioString to horaFinString // Devolver un par de fechas en lugar de una cadena
    }

    private fun showSuenioRecords() {
        val textViewSuenioRecords: TextView = findViewById(R.id.textViewSuenioRecords)
        textViewSuenioRecords.text = "Registros de sueño:\n\n"

        suenioList.forEach { suenioEntry ->
            val (startTime, endTime) = convertirMillisAFecha(suenioEntry.startTime, suenioEntry.startTime, suenioEntry.endTime)
            val duracion = suenioEntry.duracion
            val calidad = suenioEntry.calidadSuenio

            val record = "Inicio: $startTime - Fin: $endTime - Duración: $duracion - Calidad: $calidad\n\n"
            textViewSuenioRecords.append(record)
        }
    }

    fun deleteAllSuenios(view: View) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            try {
                val uid = currentUser.uid
                val reference = FirebaseDatabase.getInstance().getReference("registroSuenio").child(uid)

                reference.removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(
                            this@suenio,
                            "Todas las actividades de sueño eliminadas",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Limpiar la lista de datos
                        suenioList.clear()

                        // Eliminar los datos de SharedPreferences usando la clave del UID del usuario actual
                        sharedPreferences.edit().remove(uid).apply()

                        // Actualizar la interfaz
                        updateChart()
                        showSuenioRecords()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this@suenio,
                            "Error al eliminar las actividades de sueño: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Error al eliminar las actividades de sueño",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        } else {
            Toast.makeText(
                this,
                "Error al eliminar las actividades de sueño",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    override fun onResume() {
        super.onResume()
        // Cargar los datos de sueño desde SharedPreferences y actualizar el gráfico
        loadSuenioDataFromSharedPreferences()
    }

    private fun loadSuenioDataFromSharedPreferences() {
        val json = sharedPreferences.getString(userId, null) // Obtener los datos según el UID del usuario actual
        json?.let {
            val type = object : TypeToken<MutableList<SleepData>>() {}.type
            suenioList.clear()
            suenioList.addAll(Gson().fromJson(json, type))
        }
        updateChart()
        showSuenioRecords()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Eliminar el ValueEventListener al destruir la actividad para evitar fugas de memoria
        childEventListener?.let {
            databaseReference.removeEventListener(it)
        }
    }
}

data class SleepData(
    val startTime: Long,
    val endTime: Long,
    val duracion: String,
    val calidadSuenio: String,
    val timestamp: Long // Agregar el campo timestamp
)