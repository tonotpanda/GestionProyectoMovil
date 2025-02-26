package com.example.gestionproyecto

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class GestorTareasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val backlog = mutableListOf<String>()
    private val toDo = mutableListOf<String>()
    private val doing = mutableListOf<String>()
    private val testing = mutableListOf<String>()
    private val finished = mutableListOf<String>()
    private val columnas = mutableListOf(backlog, toDo, doing, testing, finished)

    private lateinit var proyecto: Proyecto
    private val gson = Gson()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gestor_tarea_activity)

        val buttonGuardar = findViewById<Button>(R.id.btnGuardar)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Obtener el proyecto desde el intent
        proyecto = intent.getSerializableExtra("PROYECTO_INFO") as Proyecto

        // Mostrar la información del proyecto
        findViewById<TextView>(R.id.projectName).text = proyecto.NombreProyecto
        findViewById<TextView>(R.id.projectDates).text = "Fecha inicio: ${proyecto.FechaInicio}\nFecha fin: ${proyecto.FechaFin}"
        findViewById<TextView>(R.id.projectSupervisor).text = "Supervisado por: ${proyecto.Usuarios.first()}"

        // Cargar el estado guardado de las columnas si existe
        cargarEstadoColumnas()

        // Configurar el adaptador de columnas
        val columnAdapter = ColumnAdapter(columnas, ::onTaskClick)
        recyclerView.adapter = columnAdapter

        // Configurar el botón de guardar
        buttonGuardar.setOnClickListener {
            // Guardar el estado de las columnas
            guardarEstadoColumnas()

            // Mostrar un mensaje de confirmación
            Toast.makeText(this, "Estado guardado correctamente", Toast.LENGTH_SHORT).show()

            // Crear un Intent para volver a la actividad anterior (LeerJsonActivity)
            val intent = Intent(this, LeerJsonActivity::class.java)

            // Iniciar la actividad
            startActivity(intent)

            // Finalizar la actividad actual para que no quede en la pila de actividades
            finish()
        }
    }

    private fun llenarColumnasConTareas(tareas: List<Tareas>) {
        tareas.forEach { tarea ->
            backlog.add(tarea.NombreTarea)
            tarea.Subtareas.forEach { subtarea ->
                backlog.add(subtarea)
            }
        }
    }

    private fun onTaskClick(tarea: String, fromColumnIndex: Int, view: View) {
        // Crear el PopupMenu y anclarlo al View que se hizo clic
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.column_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.move_to_backlog -> moveTask(tarea, fromColumnIndex, 0)
                R.id.move_to_todo -> moveTask(tarea, fromColumnIndex, 1)
                R.id.move_to_doing -> moveTask(tarea, fromColumnIndex, 2)
                R.id.move_to_testing -> moveTask(tarea, fromColumnIndex, 3)
                R.id.move_to_finished -> moveTask(tarea, fromColumnIndex, 4)
            }
            true
        }

        // Mostrar el PopupMenu
        popupMenu.show()
    }

    private fun moveTask(tarea: String, fromColumnIndex: Int, toColumnIndex: Int) {
        // Obtener las columnas de origen y destino
        val fromColumn = columnas[fromColumnIndex]
        val toColumn = columnas[toColumnIndex]

        // Eliminar la tarea de la columna de origen
        fromColumn.remove(tarea)

        // Agregar la tarea a la columna de destino
        toColumn.add(tarea)

        // Notificar al adaptador que los datos han cambiado
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun guardarEstadoColumnas() {
        // Crear un mapa para guardar el estado de las columnas
        val estadoColumnas = mapOf(
            "backlog" to backlog,
            "toDo" to toDo,
            "doing" to doing,
            "testing" to testing,
            "finished" to finished
        )

        // Convertir el mapa a JSON
        val json = gson.toJson(estadoColumnas)

        // Guardar el JSON en un archivo
        val file = File(filesDir, "${proyecto.NombreProyecto}_estado.json")
        file.writeText(json)
    }

    private fun cargarEstadoColumnas() {
        val file = File(filesDir, "${proyecto.NombreProyecto}_estado.json")

        if (file.exists()) {
            // Leer el JSON del archivo
            val json = file.readText()

            // Convertir el JSON a un mapa
            val tipo = object : TypeToken<Map<String, List<String>>>() {}.type
            val estadoColumnas: Map<String, List<String>> = gson.fromJson(json, tipo)

            // Cargar el estado en las columnas
            backlog.clear()
            backlog.addAll(estadoColumnas["backlog"] ?: emptyList())

            toDo.clear()
            toDo.addAll(estadoColumnas["toDo"] ?: emptyList())

            doing.clear()
            doing.addAll(estadoColumnas["doing"] ?: emptyList())

            testing.clear()
            testing.addAll(estadoColumnas["testing"] ?: emptyList())

            finished.clear()
            finished.addAll(estadoColumnas["finished"] ?: emptyList())
        } else {
            // Si no existe un estado guardado, cargar las tareas iniciales
            llenarColumnasConTareas(proyecto.Tareas)
        }
    }
}