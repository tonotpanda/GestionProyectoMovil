package com.example.gestionproyecto

import java.io.Serializable

data class TareaInfo(
    val descripcion: String,
    val fechaInicio: String,
    val fechaFin: String,
    val estado: String
): Serializable
