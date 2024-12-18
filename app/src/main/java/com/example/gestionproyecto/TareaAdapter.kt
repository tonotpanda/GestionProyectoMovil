package com.example.gestionproyecto

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
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

    // Colores asociados a las columnas
    private val columnColors = mapOf(
        0 to R.color.backlog,   // Backlog - Amarillo
        1 to R.color.todo,      // ToDo - Azul
        2 to R.color.doing,     // Doing - Naranja
        3 to R.color.testing,   // Testing - Verde claro
        4 to R.color.finished   // Finished - Verde oscuro
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tarea, parent, false)
        return TareaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        holder.taskName.text = tareas[position]

        // Obtener el color correspondiente de la columna
        val color = ContextCompat.getColor(holder.itemView.context, columnColors[columnIndex] ?: R.color.white)

        // Crear el fondo con borde negro y esquinas redondeadas
        val borderDrawable = ContextCompat.getDrawable(holder.itemView.context, R.drawable.border)

        // Crear un ColorDrawable para el color de fondo de la columna
        val colorDrawable = ColorDrawable(color)

        // Crear una capa con el borde y el color de fondo dentro
        val layers = arrayOf(colorDrawable, borderDrawable)
        val layerDrawable = LayerDrawable(layers)

        // Asegurarse de que el color de fondo no sobresalga del borde con las esquinas redondeadas
        // Ajustar el borde para que se quede dentro de las esquinas
        layerDrawable.setLayerInset(0, 4, 4, 4, 4)  // Esto asegura que el color de fondo no sobresalga
        layerDrawable.setLayerInset(1, 0, 0, 0, 0)  // El borde debe estar fuera del color de fondo

        // Establecer el LayerDrawable como el fondo del TextView
        holder.taskName.background = layerDrawable
    }

    override fun getItemCount(): Int {
        return tareas.size
    }
}
