package com.apphealthwellnessgeneral.saludbienestargeneral

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.apphealthwellnessgeneral.saludbienestargeneral.databinding.ActivityMenuBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class menu : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val loginBinding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        loginBinding.btnCerrarSesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            googleSignInClient.signOut().addOnCompleteListener {
                // Limpiar cualquier otro dato necesario
                showToast("La sesión se ha cerrado exitosamente")
                val intent1 = Intent(this, mainActivity::class.java)
                startActivity(intent1)
            }
        }

        val button = findViewById<Button>(R.id.btnCompartir)
        button.setOnClickListener { shareApp() }

    }

    fun terminarsesion(view: View){
        val intent= Intent(this, menu::class.java) .apply {  }
        startActivity(intent)
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private fun shareApp (){
        val intent= Intent().apply {
            action= Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT,"¿Quieres mejorar tu salud? Conoce ya la aplicación Salud Bienestar General. Contáctanos en: saludbienestargeneral@gmail.com")
            type= "text/plain"
        }
        val shareIntent = Intent.createChooser(intent, null)
        startActivity(shareIntent)
    }

    fun Enterejercicios(view: View){
        val intent= Intent(this, registroActividad::class.java) .apply {  }
        startActivity(intent)
    }

    fun Enteralimentacion(view: View){
        val intent= Intent(this, alimentacion::class.java) .apply {  }
        startActivity(intent)
    }

    fun Entersaludmental(view: View){
        val intent= Intent(this, peso::class.java) .apply {  }
        startActivity(intent)
    }

    fun Entercontactarprofesional(view: View){
        val intent= Intent(this, suenio::class.java) .apply {  }
        startActivity(intent)
    }

    fun Enteracercanosotros(view: View){
        val intent= Intent(this, acercaNosotros::class.java) .apply {  }
        startActivity(intent)
    }

    fun AsesoriasPersonalizas(view: View){
        val intent= Intent(this, asesoria::class.java) .apply {  }
        startActivity(intent)
    }

    fun RegistroHyP(view: View){
        val intent= Intent(this, registro_h_p::class.java) .apply {  }
        startActivity(intent)
    }
}