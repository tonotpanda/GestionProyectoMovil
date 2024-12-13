package com.example.gestionproyecto

import java.io.Serializable

data class Proyecto(
    val nombreProyecto: String,
    val tareas: List<String>,
    val subtareas: List<String>,
    val listaUsuarios: List<String>,
    val fechaInicio: String,
    val fechaFin: String
) : Serializable

