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
                    Toast.makeText(this, "Archivo JSON cargado correctamente.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, decryptedData, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun decryptFile(uri: Uri): String {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val encryptedData = inputStream?.readBytes()

        return if (encryptedData != null) {
            val encryptedString = String(encryptedData)
            decrypt(encryptedString, keyDecrypt)
        } else {
            "Error al leer el archivo"
        }
    }

    fun decrypt(encryptedData: String, key: String): String {
        try {
            val keySpec = SecretKeySpec(key.toByteArray(), "AES")
            val decodedData = Base64.decode(encryptedData, Base64.DEFAULT)
            val iv = ByteArray(16)
            System.arraycopy(decodedData, 0, iv, 0, iv.size)
            val cipherText = ByteArray(decodedData.size - iv.size)
            System.arraycopy(decodedData, iv.size, cipherText, 0, cipherText.size)

            val ivSpec = IvParameterSpec(iv)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

            val decryptedBytes = cipher.doFinal(cipherText)
            return String(decryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error de desencriptación"
        }
    }

    // Validar el usuario y la contraseña
    fun validateUser(usuarioInput: String, contrasenaInput: String) {
        try {
            val jsonArray = JSONArray(decryptedData)

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val usuario = jsonObject.getString("UsuarioNombre")
                val esDesarrollador = jsonObject.getBoolean("EsDesarrollador")
                val encryptedPassword = jsonObject.getString("Contraseña")

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

    // Desencriptar la contraseña
    fun decryptPassword(encryptedPassword: String): String {
        try {
            val decodedPassword = Base64.decode(encryptedPassword, Base64.DEFAULT)

            val iv = ByteArray(16)
            System.arraycopy(decodedPassword, 0, iv, 0, iv.size)

            val cipherText = ByteArray(decodedPassword.size - iv.size)
            System.arraycopy(decodedPassword, iv.size, cipherText, 0, cipherText.size)

            val keySpec = SecretKeySpec(keyDecrypt.toByteArray(), "AES")
            val ivSpec = IvParameterSpec(iv)

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


