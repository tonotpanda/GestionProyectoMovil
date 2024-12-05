package com.example.gestionproyecto

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    val keyDecrypt = "0123456789012345" // La clave de desencriptado (debe tener 16 caracteres para AES)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nombreUsuario = findViewById<EditText>(R.id.nombreUsuario)
        val contrasenaUsuario = findViewById<EditText>(R.id.contrasenaUsuario)
        val botonIniciarSesion = findViewById<Button>(R.id.botonIniciarSesion)
        val botonSeleccionarJson = findViewById<Button>(R.id.botonSeleccionarJson)

        // Configuración para seleccionar el archivo JSON
        botonSeleccionarJson.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/json"  // Filtrar para archivos JSON
            startActivityForResult(intent, 1)  // Código de solicitud para elegir el archivo
        }
    }

    // Este método maneja la selección del archivo JSON y lo desencripta
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val selectedUri: Uri? = data?.data
            if (selectedUri != null) {
                val decryptedData = decryptFile(selectedUri)
                showDecryptedData(decryptedData)
            }
        }
    }

    // Desencriptar el archivo JSON
    fun decryptFile(uri: Uri): String {
        // Leer el archivo en base64 desde el URI
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val encryptedData = inputStream?.readBytes()  // Leer todos los bytes del archivo

        // Desencriptar los datos
        return if (encryptedData != null) {
            val encryptedString = String(encryptedData)
            decrypt(encryptedString, keyDecrypt)  // Desencriptar la cadena en Base64
        } else {
            "Error al leer el archivo"
        }
    }

    // Desencriptar una cadena de texto
    fun decrypt(encryptedData: String, key: String): String {
        try {
            val keySpec = SecretKeySpec(key.toByteArray(), "AES")

            // Extraemos el IV de los primeros 16 bytes del texto cifrado
            val decodedData = Base64.decode(encryptedData, Base64.DEFAULT)
            val iv = ByteArray(16)  // IV de 16 bytes
            System.arraycopy(decodedData, 0, iv, 0, iv.size)

            // Los datos cifrados se encuentran después del IV
            val cipherText = ByteArray(decodedData.size - iv.size)
            System.arraycopy(decodedData, iv.size, cipherText, 0, cipherText.size)

            val ivSpec = IvParameterSpec(iv)  // Usamos el IV extraído para la desencriptación

            // Configurar el cipher para desencriptar
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

            // Desencriptamos los datos
            val decryptedBytes = cipher.doFinal(cipherText)
            val decryptedString = String(decryptedBytes)

            // Imprime el resultado desencriptado para depuración
            return decryptedString
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error de desencriptación: ${e.message}"
        }
    }

    // Mostrar el contenido del archivo desencriptado
    fun showDecryptedData(decryptedData: String) {
        try {
            Log.d("DecryptedContent", decryptedData)

            // Intentar parsear el JSON
            val jsonArray = JSONArray(decryptedData)

            // Iterar sobre los objetos JSON y desencriptar la contraseña
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val usuario = jsonObject.getString("UsuarioNombre")
                val esDesarrollador = jsonObject.getBoolean("EsDesarrollador")
                val encryptedPassword = jsonObject.getString("Contraseña")

                // Desencriptar la contraseña
                val decryptedPassword = decryptPassword(encryptedPassword)

                // Mostrar los datos de usuario con la contraseña desencriptada
                val displayData = "Usuario: $usuario\nContraseña: $decryptedPassword\nEs Desarrollador: $esDesarrollador"
                Toast.makeText(this, displayData, Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Log.e("JSONError", "Error al analizar el JSON: ${e.message}")
            Toast.makeText(this, "Error al analizar el JSON", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }


    // Desencriptar el campo "Contraseña" (base64)
    fun decryptPassword(encryptedPassword: String): String {
        try {
            // Desencriptar la contraseña usando AES
            val decodedPassword = Base64.decode(encryptedPassword, Base64.DEFAULT)
            return String(decodedPassword)  // Aquí asumo que la contraseña no está encriptada con IV
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error al desencriptar la contraseña"
        }
    }

}
