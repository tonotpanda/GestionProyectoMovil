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


class MainActivity : AppCompatActivity() {

    val keyDecrypt = "0123456789012345" // La clave de desencriptado (debe tener 16 caracteres para AES)
    val ivDecrypt = "5432109876543210" // El IV utilizado durante la encriptación

    var decryptedData: String? = null // Guardar los datos desencriptados del archivo JSON

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

        // Manejar el clic en el botón de inicio de sesión
        botonIniciarSesion.setOnClickListener {
            val usuario = nombreUsuario.text.toString()
            val contrasena = contrasenaUsuario.text.toString()

            // Verificar si los datos están completos y si el archivo JSON ha sido cargado
            if (usuario.isNotEmpty() && contrasena.isNotEmpty()) {
                if (decryptedData != null) {
                    validateUser(usuario, contrasena)
                } else {
                    Toast.makeText(this, "Por favor selecciona un archivo JSON primero.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor ingresa usuario y contraseña.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val selectedUri: Uri? = data?.data
            if (selectedUri != null) {
                decryptedData = decryptFile(selectedUri)
                if (decryptedData != "Error al leer el archivo") {
                    Log.d("Desencriptado", decryptedData.toString())  // Imprime el JSON desencriptado en el log
                    Toast.makeText(this, "Archivo JSON cargado correctamente.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, decryptedData, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Función para desencriptar el archivo
    fun decryptFile(uri: Uri): String {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val encryptedData = inputStream?.readBytes()

        return if (encryptedData != null) {
            val encryptedString = String(encryptedData, charset("UTF-8"))
            decrypt(encryptedString, keyDecrypt, ivDecrypt)
        } else {
            "Error al leer el archivo"
        }
    }

    // Función de desencriptación AES con CBC
    fun decrypt(encryptedData: String, key: String, iv: String): String {
        try {
            // Convertimos la clave y el IV a byte arrays
            val keySpec = SecretKeySpec(key.toByteArray(), "AES")
            val ivSpec = IvParameterSpec(iv.toByteArray())

            // Decodificamos la cadena Base64
            val decodedData = Base64.decode(encryptedData, Base64.DEFAULT)

            // El IV está en los primeros 16 bytes, que es lo que hemos usado en C#
            val ivBytes = ByteArray(16)
            System.arraycopy(decodedData, 0, ivBytes, 0, ivBytes.size)

            // El texto cifrado está después del IV
            val cipherText = ByteArray(decodedData.size - ivBytes.size)
            System.arraycopy(decodedData, ivBytes.size, cipherText, 0, cipherText.size)

            // Inicializamos el Cipher para AES en modo CBC con padding
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

            // Desencriptamos los bytes
            val decryptedBytes = cipher.doFinal(cipherText)

            // Retornamos los datos como un string
            return String(decryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error de desencriptación"
        }
    }

    // Función para validar el usuario y la contraseña
    fun validateUser(usuarioInput: String, contrasenaInput: String) {
        try {
            Log.d("Desencriptado", decryptedData.toString())  // Imprime el JSON desencriptado en el log

            // Parseamos el JSON desencriptado
            val jsonArray = JSONArray(decryptedData)

            // Recorremos los usuarios en el JSON
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)

                val usuario = jsonObject.optString("Nombre", "")
                val esDesarrollador = jsonObject.optBoolean("EsDesarrollador", false)
                val encryptedPassword = jsonObject.optString("Contrasena", "")

                // Desencriptar la contraseña
                val decryptedPassword = decryptPassword(encryptedPassword)

                // Comprobar si el usuario y la contraseña coinciden
                if (usuario == usuarioInput && decryptedPassword == contrasenaInput) {
                    if (esDesarrollador) {
                        // Si es desarrollador, hacer el Intent hacia la siguiente actividad
                        val intent = Intent(this, LeerJsonActivity::class.java)
                        startActivity(intent)
                        finish()  // Cerrar la actividad actual
                        return  // Salir del ciclo
                    } else {
                        Toast.makeText(this, "No tienes acceso de desarrollador.", Toast.LENGTH_SHORT).show()
                        return
                    }
                }
            }

            // Si no se encuentra el usuario o la contraseña es incorrecta
            Toast.makeText(this, "Usuario o contraseña incorrectos.", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al procesar el archivo.", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para desencriptar la contraseña
    fun decryptPassword(encryptedPassword: String): String {
        try {
            val decodedPassword = Base64.decode(encryptedPassword, Base64.DEFAULT)

            val ivBytes = ByteArray(16)
            System.arraycopy(decodedPassword, 0, ivBytes, 0, ivBytes.size)

            val cipherText = ByteArray(decodedPassword.size - ivBytes.size)
            System.arraycopy(decodedPassword, ivBytes.size, cipherText, 0, cipherText.size)

            val keySpec = SecretKeySpec(keyDecrypt.toByteArray(), "AES")
            val ivSpec = IvParameterSpec(ivDecrypt.toByteArray())

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

            val decryptedBytes = cipher.doFinal(cipherText)
            return String(decryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error al desencriptar la contraseña"
        }
    }
}
