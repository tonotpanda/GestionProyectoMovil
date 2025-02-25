package com.example.gestionproyecto

import android.os.Bundle
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GestorTareasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val backlog = mutableListOf<String>()
    private val toDo = mutableListOf<String>()
    private val doing = mutableListOf<String>()
    private val testing = mutableListOf<String>()
    private val finished = mutableListOf<String>()
    private val columnas = mutableListOf(backlog, toDo, doing, testing, finished)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gestor_tarea_activity)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val proyecto = intent.getSerializableExtra("PROYECTO_INFO") as Proyecto

        findViewById<TextView>(R.id.projectName).text = proyecto.NombreProyecto
        findViewById<TextView>(R.id.projectDates).text = "Fecha inicio: ${proyecto.FechaInicio}\nFecha fin: ${proyecto.FechaFin}"
        findViewById<TextView>(R.id.projectSupervisor).text = "Supervisado por: ${proyecto.Usuarios.first()}"

        llenarColumnasConTareas(proyecto.Tareas)

        val columnAdapter = ColumnAdapter(columnas, ::onTaskClick)
        recyclerView.adapter = columnAdapter
    }

    private fun llenarColumnasConTareas(tareas: List<Tareas>) {
        tareas.forEach { tarea ->
            backlog.add(tarea.NombreTarea)
            tarea.Subtareas.forEach { subtarea ->
                backlog.add(subtarea)
            }
        }
    }

    private fun onTaskClick(tarea: String, fromColumnIndex: Int) {
        val popupMenu = PopupMenu(this, findViewById(R.id.recyclerView))
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

        popupMenu.show()
    }

    private fun moveTask(tarea: String, fromColumnIndex: Int, toColumnIndex: Int) {
        val fromColumn = columnas[fromColumnIndex]
        val toColumn = columnas[toColumnIndex]

        fromColumn.remove(tarea)
        toColumn.add(tarea)

        (recyclerView.adapter as ColumnAdapter).notifyDataSetChanged()
    }
}