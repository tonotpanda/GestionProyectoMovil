package com.example.gestionproyecto

import java.io.Serializable

data class Tareas(
    val NombreTarea: String,
    val Subtareas: List<String> = listOf()
) : Serializable
