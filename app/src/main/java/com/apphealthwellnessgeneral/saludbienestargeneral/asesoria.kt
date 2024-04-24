package com.apphealthwellnessgeneral.saludbienestargeneral

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class asesoria : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asesoria)
    }

    fun EnterAsincronica(view: View){
        val intent= Intent(this, asesoriaA::class.java) .apply {  }
        startActivity(intent)
    }

    fun EnterSincronica(view: View){
        val intent= Intent(this, asesoriaS::class.java) .apply {  }
        startActivity(intent)
    }


}