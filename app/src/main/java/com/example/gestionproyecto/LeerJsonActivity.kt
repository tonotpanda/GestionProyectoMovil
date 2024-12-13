package com.example.gestionproyecto

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.Toast
import com.example.gestionproyecto.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.IOException
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec

class LeerJsonActivity : Activity() {

    private lateinit var btnSeleccionarJson: Button
    private lateinit var btnSalir: Button

    // Clave y IV de desencriptación (deben ser los mismos usados para encriptar)
    private val keyDecrypt = "0123456789012345"  // Clave de 16 bytes
    private val ivDecrypt = "5432109876543210"  // IV de 16 bytes

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.leer_json_activity)

        // Inicializamos los botones
        btnSeleccionarJson = findViewById(R.id.btnSeleccionarJson)
        btnSalir = findViewById(R.id.btnSalir)

        // Evento de clic para seleccionar el archivo JSON
        btnSeleccionarJson.setOnClickListener {
            // Abrir el selector de archivos
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "application/json"  // Solo archivos JSON
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            // Usamos el resultado de la actividad para obtener el archivo seleccionado
            startActivityForResult(intent, PICK_JSON_FILE)
        }

        // Evento de clic para salir
        btnSalir.setOnClickListener {
            finish() // Cerrar la aplicación
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_JSON_FILE && resultCode == RESULT_OK) {
            val fileUri: Uri? = data?.data // Obtener la URI del archivo JSON seleccionado

            fileUri?.let {
                try {
                    val jsonContent = readFile(it) // Leer el contenido del archivo JSON
                    val jsonDesencriptado = decryptJson(jsonContent) // Desencriptar el JSON
                    parseJson(jsonDesencriptado) // Parsear el contenido JSON desencriptado
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error al leer el archivo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Método para leer el contenido del archivo JSON
    @Throws(IOException::class)
    private fun readFile(fileUri: Uri): String {
        val inputStream = contentResolver.openInputStream(fileUri)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }
        reader.close()
        return stringBuilder.toString()
    }

    // Función para desencriptar el JSON (AES CBC)
    private fun decryptJson(encryptedJson: String): String {
        try {
            // Decodificar el contenido Base64
            val encryptedData = Base64.decode(encryptedJson, Base64.DEFAULT)

            // Extraer IV y texto cifrado
            val iv = IvParameterSpec(ivDecrypt.toByteArray())
            val cipherText = encryptedData.copyOfRange(16, encryptedData.size) // Excluir el IV de los primeros 16 bytes

            // Crear la clave de desencriptación
            val keySpec = SecretKeySpec(keyDecrypt.toByteArray(), "AES")

            // Inicializar el cifrador y desencriptar
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
            val decryptedBytes = cipher.doFinal(cipherText)

            // Convertir los bytes desencriptados a string y retornar
            return String(decryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al desencriptar el archivo", Toast.LENGTH_SHORT).show()
            return ""
        }
    }

    // Método para parsear el JSON desencriptado
    private fun parseJson(jsonContent: String) {
        try {
            val jsonArray = JSONArray(jsonContent)

            val proyectoNames = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val nombreProyecto = jsonObject.getString("NombreProyecto")
                proyectoNames.add(nombreProyecto)
            }

            // Mostrar un diálogo para seleccionar el proyecto
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Selecciona un Proyecto")
            builder.setItems(proyectoNames.toTypedArray()) { dialog, which ->
                // Obtener el proyecto seleccionado
                val selectedProject = jsonArray.getJSONObject(which)

                // Extraer la información del proyecto seleccionado
                val nombreProyecto = selectedProject.getString("NombreProyecto")
                val tareas = getJSONArrayAsString(selectedProject, "Tareas")
                val subtareas = getJSONArrayAsString(selectedProject, "SubTareas")
                val listaUsuarios = getJSONArrayAsString(selectedProject, "ListaUsuarios")
                val fechaInicio = selectedProject.getString("FechaInicio")
                val fechaFin = selectedProject.getString("FechaFin")

                // Crear una instancia del objeto Proyecto
                val proyecto = Proyecto(
                    nombreProyecto,
                    tareas.split(", "),  // Convertir la cadena a lista
                    subtareas.split(", "),  // Convertir la cadena a lista
                    listaUsuarios.split(", "),  // Convertir la cadena a lista
                    fechaInicio,
                    fechaFin
                )

                // Enviar los datos a la nueva actividad
                val intent = Intent(this, GestorTareasActivity::class.java)
                intent.putExtra("PROYECTO_INFO", proyecto)  // Asegúrate de que "PROYECTO_INFO" sea correcto
                startActivity(intent)
            }

            builder.show()

        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al parsear el JSON", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para convertir JSONArray a una cadena de texto
    private fun getJSONArrayAsString(jsonObject: JSONObject, key: String): String {
        return try {
            val jsonArray = jsonObject.getJSONArray(key) // Intentamos obtener el JSONArray
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list.joinToString(", ") // Convertimos el JSONArray en una cadena de texto
        } catch (e: JSONException) {
            // Si hay un error (lo que significa que el valor no es un JSONArray), lo tratamos como un String
            jsonObject.getString(key)
        }
    }

    companion object {
        const val PICK_JSON_FILE = 1
    }
}

