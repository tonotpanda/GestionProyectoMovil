package com.example.gestionproyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TareaAdapter(private val tareas: List<String>) : RecyclerView.Adapter<TareaAdapter.TareaViewHolder>() {

    inner class TareaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tareaText: TextView = itemView.findViewById(R.id.tareaText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tarea, parent, false)
        return TareaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        holder.tareaText.text = tareas[position]
    }

    override fun getItemCount(): Int {
        return tareas.size
    }
}
