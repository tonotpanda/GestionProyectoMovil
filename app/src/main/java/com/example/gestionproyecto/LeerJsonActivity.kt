package com.example.gestionproyecto

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LeerJsonActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.leer_json_activity)

        val botonSalir = findViewById<Button>(R.id.btnSalir)


        botonSalir.setOnClickListener {
            finish()
        }


    }
}