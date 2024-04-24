package com.apphealthwellnessgeneral.saludbienestargeneral

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.apphealthwellnessgeneral.saludbienestargeneral.databinding.ActivityAsesoriaABinding


class asesoriaA : AppCompatActivity() {
    lateinit var binding: ActivityAsesoriaABinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAsesoriaABinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button1.setOnClickListener {
            replaceFragment(fragA2_1())
        }

        binding.button2.setOnClickListener {
            replaceFragment(fragA2_2())
        }

        binding.button3.setOnClickListener {
            replaceFragment(fragA2_3())
        }

        binding.button4.setOnClickListener {
            replaceFragment(fragA2_4())
        }

        binding.button5.setOnClickListener {
            replaceFragment(fragA2_5())
        }

        binding.button6.setOnClickListener {
            replaceFragment(fragA2_6())
        }

        binding.button7.setOnClickListener {
            replaceFragment(fragA2_7())
        }

        binding.buttonE.setOnClickListener {
            replaceFragment(fragA2_E())
        }

        binding.buttonN.setOnClickListener {
            replaceFragment(fragA2_N())
        }

        binding.buttonP.setOnClickListener {
            replaceFragment(fragA2_P())
        }
    }

    fun EnterSincronica(view: View){
        val intent= Intent(this, asesoriaS::class.java) .apply {  }
        startActivity(intent)
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction= fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.contenedor,fragment)
        fragmentTransaction.commit()
    }
}