package com.apphealthwellnessgeneral.saludbienestargeneral

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class registro_h_p : AppCompatActivity() {

    private lateinit var editTextFechaNacimiento: EditText
    private lateinit var spinnerGenero: Spinner
    private lateinit var spinnerGrupoSanguineo: Spinner
    private lateinit var generoArray: Array<String>
    private lateinit var grupoSanguineoArray: Array<String>
    private lateinit var generoAdapter: ArrayAdapter<String>
    private lateinit var grupoSanguineoAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_h_p)

        editTextFechaNacimiento = findViewById(R.id.editTextFechaNacimiento)
        spinnerGenero = findViewById(R.id.spinnerGenero)
        spinnerGrupoSanguineo = findViewById(R.id.spinnerGrupoSanguineo)

        // Obtener los arrays de opciones de género y grupo sanguíneo
        generoArray = resources.getStringArray(R.array.opciones_genero)
        grupoSanguineoArray = resources.getStringArray(R.array.opciones_grupo_sanguineo)

        // Crear adaptadores para los spinners
        generoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, generoArray)
        grupoSanguineoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, grupoSanguineoArray)

        // Especificar los layouts de los spinners
        generoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        grupoSanguineoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Aplicar los adaptadores a los spinners
        spinnerGenero.adapter = generoAdapter
        spinnerGrupoSanguineo.adapter = grupoSanguineoAdapter

        editTextFechaNacimiento.setOnClickListener {
            mostrarSelectorFecha()
        }

        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        btnGuardar.setOnClickListener {
            guardarHistoriaClinica()
        }

        // Configurar el evento de selección del Spinner para el género
        spinnerGenero.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (position == generoArray.size - 1) { // Si se selecciona la opción "Otro"
                    mostrarDialogoGeneroPersonalizado()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Obtener y mostrar la historia clínica del usuario actualmente autenticado
        mostrarHistoriaClinicaUsuarioActual()
    }

    private fun mostrarSelectorFecha() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val fechaSeleccionada = String.format(
                    Locale.getDefault(),
                    "%04d-%02d-%02d",
                    selectedYear,
                    selectedMonth + 1,
                    selectedDay
                )
                editTextFechaNacimiento.setText(fechaSeleccionada)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun guardarHistoriaClinica() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            try {
                val uid = currentUser.uid
                val email = currentUser.email
                val reference = FirebaseDatabase.getInstance().getReference("historiasClinicas").child(uid)

                // Guardar datos en Firebase
                reference.child("Email ").setValue(email)
                reference.child("Nombre Completo ").setValue(findViewById<EditText>(R.id.editTextNombreCompleto).text.toString())
                reference.child("Fecha Nacimiento ").setValue(findViewById<EditText>(R.id.editTextFechaNacimiento).text.toString())
                reference.child("Genero ").setValue(spinnerGenero.selectedItem.toString())
                reference.child("Grupo Sanguineo ").setValue(spinnerGrupoSanguineo.selectedItem.toString())
                reference.child("Teléfono de contacto ").setValue(findViewById<EditText>(R.id.editTextTelefonoContacto).text.toString())
                reference.child("Condiciones Preexistentes ").setValue(findViewById<EditText>(R.id.editTextCondicionesPreexistentes).text.toString())
                reference.child("Cirugias anteriores ").setValue(findViewById<EditText>(R.id.editTextCirugiasAnteriores).text.toString())
                reference.child("Medicamentos ").setValue(findViewById<EditText>(R.id.editTextMedicamentos).text.toString())
                reference.child("Hábitos ").setValue(findViewById<EditText>(R.id.editTextHabitos).text.toString())
                reference.child("Alergias ").setValue(findViewById<EditText>(R.id.editTextAlergias).text.toString())
                reference.child("Reacciones Adversas ").setValue(findViewById<EditText>(R.id.editTextReaccionesAdversas).text.toString())
                reference.child("Enfermedades ").setValue(findViewById<EditText>(R.id.editTextEnfermedades).text.toString())
                reference.child("Gravedad ").setValue(findViewById<EditText>(R.id.editTextGravedad).text.toString())
                reference.child("Tratamientos ").setValue(findViewById<EditText>(R.id.editTextTratamientos).text.toString())

                // Mostrar mensaje de éxito
                Toast.makeText(this, "¡Historia clínica registrada!", Toast.LENGTH_SHORT).show()

                // Actualizar la historia clínica mostrada
                mostrarHistoriaClinicaUsuarioActual()
            } catch (e: Exception) {
                Toast.makeText(this, "¡Error al registrar la historia clínica!", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "¡Error al registrar el historial clínico!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarHistoriaClinicaUsuarioActual() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val reference = FirebaseDatabase.getInstance().getReference("historiasClinicas").child(uid)

            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val historiaClinica = StringBuilder()
                    for (child in snapshot.children) {
                        historiaClinica.append(child.key).append(": ").append(child.value).append("\n")
                    }
                    findViewById<TextView>(R.id.textViewDatosFirebase).text = historiaClinica.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar error de lectura de datos
                }
            })
        }
    }

    private fun mostrarDialogoGeneroPersonalizado() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Ingresa tu género personalizado")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("Aceptar") { _, _ ->
            val generoPersonalizado = input.text.toString()
            // Aquí puedes manejar el género personalizado, por ejemplo, guardarlo en la base de datos
            // Luego, actualiza el Spinner con el nuevo género
            generoArray[generoArray.size - 1] = generoPersonalizado // Reemplaza la opción "Otro" por el género personalizado
            generoAdapter.notifyDataSetChanged() // Notifica al adapter sobre el cambio
            spinnerGenero.setSelection(generoArray.size - 1) // Selecciona el nuevo género en el Spinner
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }

        builder.show()
    }
}
