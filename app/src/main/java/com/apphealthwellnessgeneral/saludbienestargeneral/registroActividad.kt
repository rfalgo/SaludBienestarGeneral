package com.apphealthwellnessgeneral.saludbienestargeneral


import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class registroActividad : AppCompatActivity() {

    private lateinit var txtActivities: TextView
    private lateinit var databaseReference: DatabaseReference
    private var childEventListener: ChildEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_actividad)

        txtActivities = findViewById(R.id.txtActivities)

        val btnSaveActivity = findViewById<Button>(R.id.btnSaveActivity)
        btnSaveActivity.setOnClickListener {
            saveActivity()
        }

        // Inicializar la referencia a la base de datos
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            databaseReference = FirebaseDatabase.getInstance().getReference("actividadFisica").child(uid)

            // Escuchar cambios en la base de datos y actualizar el TextView
            childEventListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    updateTxtActivities(snapshot)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    updateTxtActivities(snapshot)
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    updateTxtActivities(snapshot)
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // No es necesario para este ejemplo
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar errores si es necesario
                }
            }

            databaseReference.addChildEventListener(childEventListener!!)
        }
    }

    // Dentro de la función updateTxtActivities
    private fun updateTxtActivities(snapshot: DataSnapshot) {
        // Resto del código para agregar entradas
        val email = snapshot.child("Correo ").value?.toString() ?: ""
        val activityType = snapshot.child("Nombre de actividad ").value?.toString() ?: ""
        val duration = snapshot.child("Duración(Minutos) ").value?.toString() ?: ""
        val calories = snapshot.child("Calorias ").value?.toString() ?: ""
        val date = snapshot.child("Fecha y hora ").value?.toString() ?: ""

        if (email.isNotEmpty() && activityType.isNotEmpty() && duration.isNotEmpty() && calories.isNotEmpty() && date.isNotEmpty()) {
            val activityEntry = "Email: $email\nNombre de actividad: $activityType\nDuración: $duration minutos\nCalorías: $calories\nFecha: $date\n\n"

            // Agregar la entrada al TextView sin limpiarlo
            runOnUiThread {
                txtActivities.append(activityEntry)
            }
        }
    }

    fun deleteAllActivities(view: View) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            try {
                val uid = currentUser.uid
                val reference = FirebaseDatabase.getInstance().getReference("actividadFisica").child(uid)

                // Eliminar todos los registros bajo la referencia
                reference.removeValue()
                    .addOnSuccessListener {
                        // Vuelve a agregar el ChildEventListener
                        databaseReference.addChildEventListener(childEventListener!!)


                        Toast.makeText(
                            this@registroActividad,
                            "Todas las actividades eliminadas",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Limpiar el TextView después de la eliminación
                        txtActivities.text = "Registros de actividad física:\n\n"

                        // Detener las actualizaciones del ChildEventListener después de eliminar
                        childEventListener?.let {
                            databaseReference.removeEventListener(it)
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this@registroActividad,
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

    private fun saveActivity() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            try {
                val currentDateInMillis = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm" , Locale.getDefault())
                val formattedDateTime = dateFormat.format(Date(currentDateInMillis))

                val uid = currentUser.uid
                val email = currentUser.email

                val viewActivitiesId =
                    FirebaseDatabase.getInstance().getReference("actividadFisica").child(uid)
                        .push().key

                val reference =
                    FirebaseDatabase.getInstance().getReference("actividadFisica").child(uid)
                        .child(viewActivitiesId ?: "")

                reference.child("Correo ").setValue(email)
                reference.child("Nombre de actividad ")
                    .setValue(findViewById<EditText>(R.id.etActivityType).text.toString())
                reference.child("Duración(Minutos) ")
                    .setValue(findViewById<EditText>(R.id.etDuration).text.toString())
                reference.child("Calorias ")
                    .setValue(findViewById<EditText>(R.id.etCalories).text.toString())
                reference.child("Fecha y hora ").setValue(formattedDateTime)

                Toast.makeText(this, "Actividad física registrada", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "¡Error al registrar actividad física.!",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        } else {
            Toast.makeText(
                this,
                "¡Error al registrar actividad física.!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

