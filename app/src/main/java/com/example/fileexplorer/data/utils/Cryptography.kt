package com.example.fileexplorer.data.utils

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class Cryptography {

    private val encryptionStandard = "AES"
    private val hashFunction = "SHA-256"
    private val charset = "UTF-8"

    private fun decrypt(datos: String?, password: String): String {
        val secretKeySpec = generateKey(password)
        val cipher = Cipher.getInstance(encryptionStandard)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        val datosDecodificados = Base64.decode(datos, Base64.DEFAULT)
        val datosDesencriptadosByte = cipher.doFinal(datosDecodificados)
        return String(datosDesencriptadosByte)
    }

    private fun encrypt(datos: String, password: String): String {
        val secretKeySpec = generateKey(password)
        val cipher = Cipher.getInstance(encryptionStandard)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        val datosEncriptadosBytes = cipher.doFinal(datos.toByteArray())
        return Base64.encodeToString(datosEncriptadosBytes, Base64.DEFAULT)
    }

    private fun generateKey(password: String): SecretKeySpec {
        val sha = MessageDigest.getInstance(hashFunction)
        var key: ByteArray? = password.toByteArray(charset(charset))
        key = sha.digest(key)
        return SecretKeySpec(key, encryptionStandard)
    }

}