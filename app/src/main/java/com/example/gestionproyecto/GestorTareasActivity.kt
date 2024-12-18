package com.example.gestionproyecto

import android.content.Intent
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
        val popupMenu = PopupMenu(this, findViewById(R.id.recyclerView))
        popupMenu.menuInflater.inflate(R.menu.main_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.info_task -> {
                    // Abrir la actividad TareaDetailActivity con la información de la tarea
                    val intent = Intent(this, InfoTareaActivity::class.java)
                    intent.putExtra("TASK_INFO", tarea)  // Enviar la tarea al detalle
                    startActivity(intent)
                    true
                }
                R.id.move_task -> {
                    val movePopupMenu = PopupMenu(this, findViewById(R.id.recyclerView))
                    movePopupMenu.menuInflater.inflate(R.menu.column_menu, movePopupMenu.menu)

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
                    movePopupMenu.show()
                    true
                }
                else -> false
            }
        }

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
            (recyclerView.adapter as ColumnAdapter).notifyDataSetChanged()  // Actualizar toda la vista

            // Puedes agregar una notificación o algún mensaje para informar al usuario
            Toast.makeText(this, "Tarea movida a ${getColumnName(toColumnIndex)}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getColumnName(index: Int): String {
        return when (index) {
            0 -> "Backlog"
            1 -> "To Do"
            2 -> "Doing"
            3 -> "Testing"
            4 -> "Finished"
            else -> "Desconocido"
        }
    }

}
