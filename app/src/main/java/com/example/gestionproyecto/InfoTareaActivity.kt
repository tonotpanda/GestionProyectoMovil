package com.example.gestionproyecto

import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class InfoTareaActivity : AppCompatActivity() {

    // Variable para almacenar la información de las tareas (en memoria)
    private val PREFS_NAME = "tarea_prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.task_info)

        // Obtener el nombre de la tarea desde el Intent
        val tarea = intent.getStringExtra("TASK_INFO")
            ?: return  // Asegurarse de que el valor de la tarea no sea nulo

        // Mostrar los datos en los campos
        val descriptionEditText = findViewById<EditText>(R.id.descriptionEditText)
        val fechaInicioPicker = findViewById<DatePicker>(R.id.fechaInicio)
        val fechaFinPicker = findViewById<DatePicker>(R.id.fechaFin)
        // Configurar el botón para guardar la información
        val saveButton = findViewById<Button>(R.id.saveInfoButton)
        // Intentamos cargar la información de la tarea (si está disponible)
        val tareaInfo = getTareaInfo(tarea)

        // Si hay datos previos, los mostramos
        tareaInfo?.let {
            descriptionEditText.setText(it.descripcion)

            val fechaInicio = it.fechaInicio.split("-")
            val fechaFin = it.fechaFin.split("-")
            fechaInicioPicker.updateDate(
                fechaInicio[2].toInt(),
                fechaInicio[1].toInt() - 1,
                fechaInicio[0].toInt()
            )
            fechaFinPicker.updateDate(
                fechaFin[2].toInt(),
                fechaFin[1].toInt() - 1,
                fechaFin[0].toInt()
            )
        }


        saveButton.setOnClickListener {
            // Obtener los datos introducidos por el usuario
            val descripcion = descriptionEditText.text.toString()
            val fechaInicio =
                "${fechaInicioPicker.dayOfMonth}-${fechaInicioPicker.month + 1}-${fechaInicioPicker.year}"
            val fechaFin =
                "${fechaFinPicker.dayOfMonth}-${fechaFinPicker.month + 1}-${fechaFinPicker.year}"
            val estado =
                "Amarillo" // Este valor puede cambiar dependiendo del botón de estado que selecciones

            // Guardar la información actualizada de la tarea
            val tareaInfo = TareaInfo(descripcion, fechaInicio, fechaFin, estado)
            saveTareaInfo(tarea, tareaInfo)  // Guardar la tarea (en SharedPreferences)

            // Mostrar un mensaje de confirmación
            Toast.makeText(this, "Información de la tarea guardada", Toast.LENGTH_SHORT).show()

            // Regresar a la actividad anterior
            finish()
        }
    }

    // Método para cargar la información de la tarea desde SharedPreferences
    private fun getTareaInfo(tarea: String): TareaInfo? {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val descripcion = sharedPreferences.getString("${tarea}_descripcion", null)
        val fechaInicio = sharedPreferences.getString("${tarea}_fechaInicio", null)
        val fechaFin = sharedPreferences.getString("${tarea}_fechaFin", null)
        val estado = sharedPreferences.getString("${tarea}_estado", null)

        // Si alguno de los valores es null, no hay información guardada
        return if (descripcion != null && fechaInicio != null && fechaFin != null && estado != null) {
            TareaInfo(descripcion, fechaInicio, fechaFin, estado)
        } else {
            null
        }
    }

    // Método para guardar la información de la tarea en SharedPreferences
    private fun saveTareaInfo(tarea: String, tareaInfo: TareaInfo) {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("${tarea}_descripcion", tareaInfo.descripcion)
        editor.putString("${tarea}_fechaInicio", tareaInfo.fechaInicio)
        editor.putString("${tarea}_fechaFin", tareaInfo.fechaFin)
        editor.putString("${tarea}_estado", tareaInfo.estado)
        editor.apply()
    }
}
