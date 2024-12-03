package com.example.gestionproyecto

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GestorTareasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val backlog = mutableListOf<String>()
    private val toDo = listOf<String>()
    private val doing = listOf<String>()
    private val testing = listOf<String>()
    private val finished = listOf<String>()
    private lateinit var projectName: String
    private lateinit var projectStartDate: String
    private lateinit var projectEndDate: String
    private lateinit var projectSupervisor: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gestor_tarea_activity)

        recyclerView = findViewById(R.id.recyclerView)

        // Configuramos el RecyclerView para mostrar las columnas
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Obtener el objeto Proyecto pasado desde la actividad anterior
        val proyecto = intent.getSerializableExtra("PROYECTO_INFO") as Proyecto

        // Mostrar la información del proyecto
        findViewById<TextView>(R.id.projectName).text = proyecto.nombreProyecto
        findViewById<TextView>(R.id.projectDates).text = "Fecha inicio: ${proyecto.fechaInicio}\nFecha fin: ${proyecto.fechaFin}"
        findViewById<TextView>(R.id.projectSupervisor).text = "Supervisado por: ${proyecto.listaUsuarios.first()}" // Mostrar primer usuario

        // Llenar el Backlog con tareas y subtareas
        backlog.addAll(proyecto.tareas)
        backlog.addAll(proyecto.subtareas)

        // Crear un conjunto de listas para las diferentes etapas
        val tareasColumns = listOf(backlog, toDo, doing, testing, finished)

        val columnAdapter = ColumnAdapter(tareasColumns)
        recyclerView.adapter = columnAdapter
    }
}
