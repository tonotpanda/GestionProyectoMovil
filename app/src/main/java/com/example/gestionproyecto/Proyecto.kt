package com.example.gestionproyecto

import java.io.Serializable

data class Proyecto(
    val NombreProyecto: String,
    val Tareas: List<Tareas> = listOf(),
    val FechaInicio: String,
    val FechaFin: String,
    val Usuarios: List<String> = listOf()
) : Serializable

