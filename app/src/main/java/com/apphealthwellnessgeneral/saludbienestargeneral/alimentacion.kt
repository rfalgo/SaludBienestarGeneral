package com.apphealthwellnessgeneral.saludbienestargeneral

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class alimentacion : AppCompatActivity() {

    private lateinit var spinnerTipoComida: Spinner
    private lateinit var tipoComidaArray: Array<String>
    private lateinit var tipoComidaAdapter: ArrayAdapter<String>

    private lateinit var mContext: Context
    private lateinit var recyclerView: RecyclerView
    private lateinit var dietEntryAdapter: dietEntryAdapter
    private val dietEntries = mutableListOf<dietEntryModel>()

    private lateinit var foodNameEditText: EditText
    private lateinit var caloriesEditText: EditText

    private lateinit var portionsEditText: EditText
    private lateinit var recommendationText: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alimentacion)
        mContext = this

        spinnerTipoComida = findViewById(R.id.spinnerMealType)
        tipoComidaArray = resources.getStringArray(R.array.tipo_comida)
        tipoComidaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipoComidaArray)
        tipoComidaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoComida.adapter = tipoComidaAdapter

        foodNameEditText = findViewById(R.id.editTextFoodName)
        caloriesEditText = findViewById(R.id.editTextCaloriesEntry)

        recyclerView = findViewById(R.id.recyclerViewDietEntries)
        recyclerView.layoutManager = LinearLayoutManager(mContext)
        dietEntryAdapter = dietEntryAdapter(dietEntries)
        recyclerView.adapter = dietEntryAdapter

        portionsEditText = findViewById(R.id.editTextPortions)
        recommendationText = findViewById(R.id.textRecommendation)

        loadDietEntries()

        val btnDeleteEntry: Button = findViewById(R.id.btnDeleteDietEntry)
        btnDeleteEntry.setOnClickListener {
            deleteLastEntry()
        }

        // Botón para guardar la entrada de dieta
        val btnSaveEntry: Button = findViewById(R.id.btnSaveDietEntry)
        btnSaveEntry.setOnClickListener {
            saveDietEntry()
        }
    }

    // Obtén la referencia de la base de datos
    private val database = FirebaseDatabase.getInstance().reference
    // Obtener el UID del usuario autenticado
    private val uid = FirebaseAuth.getInstance().currentUser?.uid

    private fun loadDietEntries() {
        // Verificar si el usuario está autenticado
        if (uid != null) {
            // Utilizar el UID del usuario para recuperar las entradas de dieta asociadas
            database.child("diet_entries").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val gson = Gson()
                    val json: String? = snapshot.child("entries").value as? String
                    val userEmail: String? = snapshot.child("userEmail").value as? String
                    val type = object : TypeToken<MutableList<dietEntryModel>>() {}.type

                    if (json != null && userEmail != null) {
                        // Accede al correo electrónico del usuario aquí (userEmail)
                        // ...

                        val savedEntries: MutableList<dietEntryModel> = gson.fromJson(json, type)
                        dietEntries.clear()
                        dietEntries.addAll(savedEntries)
                        dietEntryAdapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar el error
                }
            })
        }
    }

    private fun saveDietEntries() {
        // Verificar si el usuario está autenticado
        if (uid != null) {
            // Utilizar el UID del usuario para guardar las entradas de dieta asociadas
            val gson = Gson()
            val json: String = gson.toJson(dietEntries)

            val userEmail = FirebaseAuth.getInstance().currentUser?.email

            if (userEmail != null) {
                val dietEntryData = mapOf(
                    "entries" to json,
                    "userEmail" to userEmail
                )

                // Incluir la porción en los datos guardados
                database.child("diet_entries").child(uid).setValue(dietEntryData)
                // Guardar la porción adicionalmente
                dietEntries.forEachIndexed { index, entry ->

                }
            }
        }
    }

    private fun saveDietEntry() {
        val foodName = foodNameEditText.text.toString()
        val calories = caloriesEditText.text.toString().toIntOrNull() ?: 0
        val portion = portionsEditText.text.toString() // Obtener la porción del EditText

        if (foodName.isNotEmpty()) {
            val timestamp = System.currentTimeMillis()
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

            // Obtenemos una recomendación aleatoria basada en el tipo de comida seleccionada
            val mealType = spinnerTipoComida.selectedItem.toString()
            val randomRecommendation = getRecommendation(mealType)

            val newEntry = dietEntryModel(foodName, mealType, calories, portion, timestamp.toString(), date, time, randomRecommendation)
            dietEntries.add(newEntry)
            dietEntryAdapter.notifyDataSetChanged()
            saveDietEntries()

            // Asignar la recomendación obtenida al TextView correspondiente
            recommendationText.text = randomRecommendation

            Toast.makeText(mContext, "Entrada de dieta guardada", Toast.LENGTH_SHORT).show()

            foodNameEditText.text.clear()
            caloriesEditText.text.clear()
            portionsEditText.text.clear() // Limpiar el EditText de la porción después de guardarlo
        } else {
            Toast.makeText(mContext, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getRecommendation(foodName: String): String {
        // Mapa de recomendaciones basadas en el tipo de comida
        val recommendations = mapOf(
            "Desayuno" to "Un desayuno equilibrado puede incluir alimentos como huevos, queso, avena, pan integral y yogur natural.",
            "Almuerzo" to "Un almuerzo saludable podría constar de proteínas magras como pollo o pescado, junto con una porción de vegetales con un tipo de carbohidrato complejo.",
            "Cena" to "La cena puede incluir una porción adecuada de proteínas, como pescado o tofu, junto con verduras y una porción controlada de carbohidratos complejos.",
            "Merienda" to "Una merienda nutritiva puede incluir frutos secos, frutas frescas o yogur natural.",
            "Media Mañana" to "Para una media mañana saludable, considera frutas frescas, yogur griego o galletas integrales.",
            "Antes de entrenar" to "Antes de hacer ejercicio, es bueno consumir una pequeña cantidad de carbohidratos y proteínas para obtener energía, como un plátano con mantequilla de maní.",
            "Después de entrenar" to "Después de hacer ejercicio, es importante reponer proteínas y carbohidratos, como un batido de proteínas con plátano y espinacas."
        )

        // Verifica si el tipo de comida está en las recomendaciones, si no está, elige una recomendación aleatoria del mapa
        return recommendations[foodName] ?: recommendations.values.random()
    }

    private fun deleteLastEntry() {
        if (dietEntries.isNotEmpty()) {
            dietEntries.removeAt(dietEntries.size - 1)
            dietEntryAdapter.notifyDataSetChanged()
            saveDietEntries()
            Toast.makeText(mContext, "Última entrada eliminada", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(mContext, "No hay entradas para eliminar", Toast.LENGTH_SHORT).show()
        }
    }
}