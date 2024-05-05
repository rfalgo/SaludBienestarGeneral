package com.apphealthwellnessgeneral.saludbienestargeneral

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.apphealthwellnessgeneral.saludbienestargeneral.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

enum class ProviderType {
    BASIC
}

class mainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val loginBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        auth= FirebaseAuth.getInstance()
        title = "Autentificacion"

        // Verificar si el usuario ya está autenticado
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // El usuario ya está autenticado, ir directamente al menú
            startMenuActivity()
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        loginBinding.btnRegistrarse.setOnClickListener {
            if (loginBinding.digitarCorreo.text.isNotEmpty() && loginBinding.digitarContrasenia.text.isNotEmpty())
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(
                        loginBinding.digitarCorreo.text.toString(),
                        loginBinding.digitarContrasenia.text.toString()
                    ).addOnCompleteListener {

                        if (it.isSuccessful) {
                            Toast.makeText(
                                this,
                                "¡Registro Exitoso!, la información es ingresada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                        } else {
                            showAlert()
                        }
                    }
            else
                Toast.makeText(
                    this,
                    "Llenar los campos o Ingresar información correcta",
                    Toast.LENGTH_SHORT
                ).show()
        }

        loginBinding.btnIniciarSesion.setOnClickListener {
            if (loginBinding.digitarContrasenia.text.isNotEmpty() && loginBinding.digitarCorreo.text.isNotEmpty())
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(
                        loginBinding.digitarCorreo.text.toString(),
                        loginBinding.digitarContrasenia.text.toString()
                    ).addOnCompleteListener {

                        if (it.isSuccessful) {
                            Toast.makeText(
                                this,
                                "¡Hola de nuevo!, la información es ingresada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            showHome(it.result?.user?.email ?: "", ProviderType.BASIC)

                        } else {
                            showAlert()
                        }
                    }
            else
                Toast.makeText(
                    this,
                    "Llenar los campos o Ingresar información correcta",
                    Toast.LENGTH_SHORT
                ).show()
        }

        loginBinding.btnOlvidoContrasenia.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Ingrese su correo de registro: ")
            val view = layoutInflater.inflate(R.layout.restablecer_contrasenia, null)
            val username = view.findViewById<EditText>(R.id.ingresarCorreo_RP)
            builder.setView(view)
            builder.setPositiveButton("Restablecer Contraseña", DialogInterface.OnClickListener { _, _ ->
                forgotPassword(username)
            })
            builder.setNegativeButton("Cerrar", DialogInterface.OnClickListener { _, _ -> })
            builder.show()
        }

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<Button>(R.id.GButton).setOnClickListener {
            googleSignIn()
        }

    }

    private fun startMenuActivity() {
        val intent = Intent(this, menu::class.java)
        startActivity(intent)
        finish()  // Opcional: finalizar la actividad actual para quitarla de la pila de actividades
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private fun googleSignIn()  {

        val signInClient = googleSignInClient.signInIntent
        launcher.launch(signInClient)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            manageResults(task)
        }
    }

    private fun manageResults(task: Task<GoogleSignInAccount>) {
        val account: GoogleSignInAccount? = task.result

        if (account != null) {
            // Realiza las acciones necesarias para la cuenta de Google
            // ...

            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener {
                if (task.isSuccessful) {
                    val intent = Intent(this, menu::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Hola :)", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun forgotPassword(username: EditText) {

        if (username.text.toString().isEmpty()) {
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(username.text.toString()).matches()) {
            return
        }

        auth.sendPasswordResetEmail(username.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Revise su correo de registro.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Error de autentificación, verifique los datos ingresados.")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String, provider: ProviderType) {
        val homeIntent = Intent(this, menu::class.java).apply {
            putExtra("Correo", email)
            putExtra("Contraseña", provider.name)
        }
        startActivity(homeIntent)
    }

}
