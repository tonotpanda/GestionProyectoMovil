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
import org.json.JSONArray
import org.json.JSONException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class LeerJsonActivity : Activity() {

    private lateinit var btnSeleccionarJson: Button
    private lateinit var btnSalir: Button

    private val keyDecrypt = "0123456789012345"
    private val ivDecrypt = "5432109876543210"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.leer_json_activity)

        btnSeleccionarJson = findViewById(R.id.btnSeleccionarJson)
        btnSalir = findViewById(R.id.btnSalir)

        btnSeleccionarJson.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "application/json"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(intent, PICK_JSON_FILE)
        }

        btnSalir.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_JSON_FILE && resultCode == RESULT_OK) {
            val fileUri: Uri? = data?.data

            fileUri?.let {
                try {
                    val jsonContent = readFile(it)
                    val jsonDesencriptado = decryptJson(jsonContent)
                    parseJson(jsonDesencriptado)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error al leer el archivo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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

    private fun decryptJson(encryptedJson: String): String {
        try {
            val encryptedData = Base64.decode(encryptedJson, Base64.DEFAULT)
            val iv = IvParameterSpec(ivDecrypt.toByteArray())
            val cipherText = encryptedData.copyOfRange(16, encryptedData.size)
            val keySpec = SecretKeySpec(keyDecrypt.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
            val decryptedBytes = cipher.doFinal(cipherText)
            return String(decryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al desencriptar el archivo", Toast.LENGTH_SHORT).show()
            return ""
        }
    }

    private fun parseJson(jsonContent: String) {
        try {
            val jsonArray = JSONArray(jsonContent)
            val proyectoNames = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val nombreProyecto = jsonObject.getString("NombreProyecto")
                proyectoNames.add(nombreProyecto)
            }

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Selecciona un Proyecto")
            builder.setItems(proyectoNames.toTypedArray()) { dialog, which ->
                val selectedProject = jsonArray.getJSONObject(which)
                val nombreProyecto = selectedProject.getString("NombreProyecto")
                val tareas = mutableListOf<Tareas>()
                val tareasArray = selectedProject.getJSONArray("Tareas")
                for (j in 0 until tareasArray.length()) {
                    val tareaObject = tareasArray.getJSONObject(j)
                    val nombreTarea = tareaObject.getString("NombreTarea")
                    val subtareas = mutableListOf<String>()
                    val subtareasArray = tareaObject.getJSONArray("Subtareas")
                    for (k in 0 until subtareasArray.length()) {
                        subtareas.add(subtareasArray.getString(k))
                    }
                    tareas.add(Tareas(nombreTarea, subtareas))
                }

                val usuarios = mutableListOf<String>()
                val usuariosArray = selectedProject.getJSONArray("Usuarios")
                for (j in 0 until usuariosArray.length()) {
                    usuarios.add(usuariosArray.getString(j))
                }

                val fechaInicio = selectedProject.getString("FechaInicio")
                val fechaFin = selectedProject.getString("FechaFin")

                val proyecto = Proyecto(nombreProyecto, tareas, fechaInicio, fechaFin, usuarios)

                val intent = Intent(this, GestorTareasActivity::class.java)
                intent.putExtra("PROYECTO_INFO", proyecto)
                startActivity(intent)
            }

            builder.show()

        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al parsear el JSON", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val PICK_JSON_FILE = 1
    }
}