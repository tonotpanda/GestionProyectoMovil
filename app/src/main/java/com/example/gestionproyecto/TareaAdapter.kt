package com.example.gestionproyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TareaAdapter(
    private val tareas: MutableList<String>,
    private val columnIndex: Int,
    private val onTaskClick: (String, Int, Int) -> Unit  // Recibe el índice de la columna y el índice de la tarea
) : RecyclerView.Adapter<TareaAdapter.TareaViewHolder>() {

    inner class TareaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskName: TextView = itemView.findViewById(R.id.tareaText)

        init {
            itemView.setOnClickListener {
                // Llama al callback cuando se hace clic en una tarea
                onTaskClick(tareas[adapterPosition], columnIndex, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tarea, parent, false)
        return TareaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        holder.taskName.text = tareas[position]
    }

    override fun getItemCount(): Int {
        return tareas.size
    }
}
