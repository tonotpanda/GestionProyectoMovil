package com.example.gestionproyecto

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ColumnAdapter(
    private val columnas: MutableList<MutableList<String>>,
    private val onTaskClick: (String, Int) -> Unit  // Acepta la función onTaskClick
) : RecyclerView.Adapter<ColumnAdapter.ColumnViewHolder>() {

    inner class ColumnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val columnTitle: TextView = itemView.findViewById(R.id.columnTitle)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColumnViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.column_layout, parent, false)
        return ColumnViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColumnViewHolder, position: Int) {
        val columnName = when (position) {
            0 -> "Backlog"
            1 -> "To Do"
            2 -> "Doing"
            3 -> "Testing"
            4 -> "Finished"
            else -> ""
        }
        holder.columnTitle.text = columnName

        // Verificar el contenido de la columna
        Log.d("ColumnAdapter", "Tareas en columna $columnName: ${columnas[position]}")

        // Configurar el RecyclerView para cada columna con las tareas correspondientes
        val taskAdapter = TareaAdapter(columnas[position], onTaskClick) // Pasar la función aquí
        holder.recyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.recyclerView.adapter = taskAdapter
    }


    override fun getItemCount(): Int {
        return columnas.size
    }
}


