package com.apphealthwellnessgeneral.saludbienestargeneral

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.auth.FirebaseAuth  // Add this import
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.pow

class peso : AppCompatActivity() {

    // Declare currentUser as a class-level property
    private var currentUser: FirebaseUser? = null

    private var childEventListener: ChildEventListener? = null

    private lateinit var editTextWeight: EditText
    private lateinit var editTextHeight: EditText
    private lateinit var btnCalculateBMI: Button
    private lateinit var textViewResult: TextView
    private lateinit var barChart: BarChart

    private val weightList =
        mutableListOf<weightEntry>() // Lista de pesos, IMC, categorías y fechas

    private lateinit var databaseReference: DatabaseReference
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_peso)


        // Assign the current user
        currentUser = FirebaseAuth.getInstance().currentUser

        // Inicializar Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().reference


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

        val currentUser = FirebaseAuth.getInstance().currentUser
        userId = currentUser?.uid ?: ""

        // Imprimir el correo electrónico del usuario en la consola de Android Studio
        Log.d("FirebaseUser", "Correo del usuario: ${currentUser?.email}")

        // Obtener y mostrar el correo electrónico del usuario en la consola de Firebase
        currentUser?.email?.let {
            Log.d("FirebaseUser", "Correo del usuario: $it")
        }

        editTextWeight = findViewById(R.id.editTextWeight)
        editTextHeight = findViewById(R.id.editTextHeight)
        btnCalculateBMI = findViewById(R.id.btnCalculateBMI)
        textViewResult = findViewById(R.id.textViewResult)
        barChart = findViewById(R.id.barChart)

        btnCalculateBMI.setOnClickListener {
            calculateBMI()
        }

        val btnDeleteRecords: Button = findViewById(R.id.btnDeleteRecords)
        btnDeleteRecords.setOnClickListener {

            deleteAllActivities()


        }

        updateChart() // Actualizar gráfico al iniciar la actividad
    }

    private fun calculateBMI() {
        saveWeight()


        val weightString = editTextWeight.text.toString()
        val heightString = editTextHeight.text.toString()

        if (weightString.isNotEmpty() && heightString.isNotEmpty()) {
            val weight = weightString.toDouble()
            val height = heightString.toDouble()

            val bmi = weight / (height / 100).pow(2)

            displayResult(bmi)
        } else {
            textViewResult.text = "Por favor, completa ambos campos."
        }
    }

    private fun displayResult(bmi: Double) {
        val resultText = "Tu IMC es: %.2f".format(bmi)
        val category = getBMICategory(bmi)
        textViewResult.text = "$resultText\nCategoría: $category"
    }

    private fun saveWeight() {
        val weightString = editTextWeight.text.toString()
        val heightString = editTextHeight.text.toString()

        try {
            val weight = weightString.toDouble()
            val height = heightString.toDouble()

            val bmi = weight / (height / 100).pow(2)
            val roundedBMI =
                String.format(Locale.US, "%.2f", bmi) // Redondear el valor de bmi a dos decimales

            val category = getBMICategory(bmi)

            val currentDateInMillis = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(currentDateInMillis))

            val weightEntry =
                weightEntry(weight.toFloat(), roundedBMI.toFloat(), category, currentDateInMillis, currentUser?.email ?: "")
            val weightEntryReference = databaseReference.child("weightEntries").child(userId).push()

            weightEntryReference.child("email").setValue(currentUser?.email)
            weightEntryReference.child("dateAndTime").setValue(formattedDate)
            weightEntryReference.child("weight").setValue(weightEntry.weight)
            weightEntryReference.child("bmi")
                .setValue(weightEntry.bmi.toString()) // Guardar el valor redondeado como una cadena
            weightEntryReference.child("category").setValue(weightEntry.category)

            weightList.add(weightEntry)

            val sharedPreferences = getSharedPreferences("WeightData", MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            // Convertir weightList a formato JSON y guardar en SharedPreferences
            val gson = Gson()
            val json = gson.toJson(weightList)
            editor.putString("weightData", json)
            editor.apply()

            updateChart()
            displayResult(bmi)
        } catch (e: NumberFormatException) {
            textViewResult.text = "Por favor, introduce un valor válido para peso y estatura."
        }
    }

    private fun updateChart() {
        val barEntries = mutableListOf<BarEntry>()
        val colors = mutableListOf<Int>()

        weightList.forEachIndexed { index, weightEntry ->
            val weight = weightEntry.weight
            val category = weightEntry.category
            barEntries.add(BarEntry(index.toFloat(), weight))

            val color = when (category) {
                "Bajo peso" -> Color.BLUE
                "Peso normal" -> Color.GREEN
                "Sobrepeso" -> Color.YELLOW
                "Obesidad" -> Color.RED
                else -> Color.GRAY
            }
            colors.add(color)
        }

        val barDataSet = BarDataSet(barEntries, "IMC")
        barDataSet.colors = colors // Establecer colores para cada barra según la categoría
        barDataSet.valueTextColor = Color.WHITE // Color del texto en las barras
        barDataSet.valueTextSize = 9f // Tamaño del texto en las barras

        val data = BarData(barDataSet)
        barChart.data = data
        barChart.description.isEnabled = false
        barChart.invalidate()

        // Configurar el eje X para no mostrar los valores
        val xAxis = barChart.xAxis
        xAxis.isEnabled = false // Deshabilitar la visualización de los números en el eje X
        xAxis.setDrawGridLines(false) // Opcional: eliminar las líneas del eje X
        xAxis.setDrawAxisLine(false) // Opcional: eliminar la línea del eje X


        val yAxis = barChart.axisLeft // Obtener el eje Y izquierdo

        // Configurar el color de los números en el eje Y
        yAxis.textColor = Color.WHITE // Establecer el color blanco para los números en el eje Y
        yAxis.textSize = 11f // Opcional: establecer el tamaño de fuente para los números


        // Eliminación del eje Y derecho (yAxisRight)
        val yAxisRight = barChart.axisRight
        yAxisRight.isEnabled = false

        // Configurar el color del texto de los valores (números) en las barras y el texto del IMC en las etiquetas
        barDataSet.valueTextColor =
            Color.WHITE // Establecer el color del texto (números y texto del IMC)


        showWeightRecords() // Llama a una función para mostrar los registros de peso y fecha
    }

    // Registros de peso en la interfaz del usuario
    private fun showWeightRecords() {
        val textViewWeightRecords: TextView = findViewById(R.id.textViewWeightRecords)
        textViewWeightRecords.text = "Registros de peso:\n\n"

        weightList.forEach { weightEntry ->
            val weight = weightEntry.weight
            val bmi = String.format("%.2f", weightEntry.bmi)
            val category = weightEntry.category
            val dateInMillis = weightEntry.dateAndTime

            // Use the actual date of the record instead of the current timestamp
            val dateString = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(dateInMillis))

            val record =
                "Fecha: $dateString - Peso: $weight kg - IMC: $bmi - Categoría: $category\n\n"
            textViewWeightRecords.append(record)
        }
    }


    private fun getBMICategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Bajo peso"
            bmi < 24.9 -> "Peso normal"
            bmi < 29.9 -> "Sobrepeso"
            else -> "Obesidad"
        }
    }

    override fun onResume() {
        super.onResume()
        loadWeightDataFromFirebase() // Load data from Firebase when the activity is resumed
        updateChart()
    }


    fun deleteAllActivities() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            try {
                val uid = currentUser.uid
                val reference = FirebaseDatabase.getInstance().getReference("weightEntries").child(uid)

                // Eliminar todos los registros bajo la referencia
                reference.removeValue()
                    .addOnSuccessListener {
                        // Vuelve a agregar el ChildEventListener
                        databaseReference.addChildEventListener(childEventListener!!)


                        Toast.makeText(
                            this@peso,
                            "Todas las actividades eliminadas",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Limpiar el TextView después de la eliminación
                        textViewResult.text = "Registros de peso:\n\n"

                        // Detener las actualizaciones del ChildEventListener después de eliminar
                        childEventListener?.let {
                            databaseReference.removeEventListener(it)
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this@peso,
                            "¡Error al eliminar las actividades: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "¡Error al eliminar las actividades!",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        } else {
            Toast.makeText(
                this,
                "¡Error al eliminar las actividades!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun loadWeightDataFromFirebase() {
        currentUser?.let { user ->
            val databaseReference = FirebaseDatabase.getInstance().getReference("weightEntries").child(userId)
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    weightList.clear() // Clear existing data
                    for (childSnapshot in snapshot.children) {
                        val dateString = childSnapshot.child("dateAndTime").getValue(String::class.java) ?: ""
                        if (dateString.isNotEmpty()) {
                            val weight = childSnapshot.child("weight").getValue(Float::class.java) ?: 0f
                            val bmi = childSnapshot.child("bmi").getValue(String::class.java) ?: "0.0"
                            val category = childSnapshot.child("category").getValue(String::class.java) ?: ""

                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val date = dateFormat.parse(dateString)

                            val weightEntry = weightEntry(weight, bmi.toFloat(), category, date?.time ?: 0, currentUser!!.email ?: "")
                            weightList.add(weightEntry) // Add to the list
                        }
                    }
                    updateChart() // Update the chart based on the new data
                    showWeightRecords() // Show weight records in the UI
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database read errors
                    error.toException().printStackTrace()
                }
            })
        }
    }

}