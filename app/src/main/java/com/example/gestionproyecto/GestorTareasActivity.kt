package com.example.gestionproyecto

import android.os.Bundle
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
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

    // Lista de columnas donde cada una contiene tareas
    private val columnas = mutableListOf(backlog, toDo, doing, testing, finished)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gestor_tarea_activity)

        recyclerView = findViewById(R.id.recyclerView)
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

        // Configurar el Adapter
        val columnAdapter = ColumnAdapter(columnas, ::onTaskClick)  // Pasa la función al adaptador
        recyclerView.adapter = columnAdapter
    }

    // Función que se llama cuando se hace clic en una tarea
    private fun onTaskClick(tarea: String, fromColumnIndex: Int, taskIndex: Int) {
        // Mostrar un PopupMenu para que el usuario elija la acción
        val popupMenu = PopupMenu(this, findViewById(R.id.recyclerView))
        popupMenu.menuInflater.inflate(R.menu.main_menu, popupMenu.menu)  // Usamos el menú principal con las opciones "Info Tarea" y "Mover Tarea"

        // Lógica para manejar la selección del menú
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                // Opción "Info Tarea" - No hace nada por ahora
                R.id.info_task -> {
                    // De momento, no hacemos nada al seleccionar "Info Tarea"
                    Toast.makeText(this, "Información de la tarea: $tarea", Toast.LENGTH_SHORT).show()
                    true
                }
                // Opción "Mover Tarea" -> Mostrar submenú para mover tarea
                R.id.move_task -> {
                    // Mostrar el submenú con las opciones para mover la tarea
                    val movePopupMenu = PopupMenu(this, findViewById(R.id.recyclerView))
                    movePopupMenu.menuInflater.inflate(R.menu.column_menu, movePopupMenu.menu)  // Usamos el menú de mover tarea

                    movePopupMenu.setOnMenuItemClickListener { subItem ->
                        when (subItem.itemId) {
                            R.id.move_to_backlog -> moveTask(tarea, fromColumnIndex, 0)
                            R.id.move_to_todo -> moveTask(tarea, fromColumnIndex, 1)
                            R.id.move_to_doing -> moveTask(tarea, fromColumnIndex, 2)
                            R.id.move_to_testing -> moveTask(tarea, fromColumnIndex, 3)
                            R.id.move_to_finished -> moveTask(tarea, fromColumnIndex, 4)
                        }
                        true
                    }
                    movePopupMenu.show()  // Mostrar el submenú
                    true
                }
                else -> false
            }
        }

        // Mostrar el menú principal
        popupMenu.show()
    }

    // Función que mueve la tarea de una columna a otra
    private fun moveTask(tarea: String, fromColumnIndex: Int, toColumnIndex: Int) {
        val fromColumn = columnas[fromColumnIndex]
        val toColumn = columnas[toColumnIndex]

        // Verificar si la tarea ya está en la columna de destino
        if (!toColumn.contains(tarea)) {
            // Eliminar la tarea de la columna de origen
            fromColumn.remove(tarea)

            // Agregar la tarea a la columna de destino
            toColumn.add(tarea)

            // Notificar que se debe actualizar el RecyclerView
            (recyclerView.adapter as ColumnAdapter).notifyDataSetChanged()  // Notificar que toda la vista debe actualizarse
        }
    }
}
