package com.example.gestionproyecto

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nombreUsuario = findViewById<EditText>(R.id.nombreUsuario)
        val contrasenaUsuario = findViewById<EditText>(R.id.contrasenaUsuario)
        val botonIniciarSesion = findViewById<Button>(R.id.botonIniciarSesion)

        val usuarioAutorizado = "prueba"
        val contrasenaAutorizada = "123"

        botonIniciarSesion.setOnClickListener {
            val nombre = nombreUsuario.text.toString()
            val contrasena = contrasenaUsuario.text.toString()

            // Verifica si les dades introduïdes coincideixen amb les dades autoritzades
            if (nombre == usuarioAutorizado && contrasena == contrasenaAutorizada) {
                Toast.makeText(this, "Bienvenido, $nombre!", Toast.LENGTH_SHORT).show()

                // Iniciar siguiente actividad
                val intent = Intent(this, LeerJsonActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Usuari o contraseña incorrecto", Toast.LENGTH_SHORT).show()
            }
        }



    }
}