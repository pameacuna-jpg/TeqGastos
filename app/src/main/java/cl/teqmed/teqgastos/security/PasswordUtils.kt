
package cl.teqmed.teqgastos.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordUtils {

    private const val ITERACIONES = 210000
    private const val LONGITUD_CLAVE = 256
    private const val LONGITUD_SAL = 16

    fun crearHash(password: String): String {
        val sal = ByteArray(LONGITUD_SAL)
        SecureRandom().nextBytes(sal)

        val hash = calcularHash(password, sal, ITERACIONES)

        return "$ITERACIONES:" +
                "${Base64.getEncoder().encodeToString(sal)}:" +
                Base64.getEncoder().encodeToString(hash)
    }

    fun verificarPassword(
        password: String,
        hashGuardado: String
    ): Boolean {
        return try {
            val partes = hashGuardado.split(":")
            if (partes.size != 3) return false

            val iteraciones = partes[0].toInt()
            if (iteraciones < 1 || iteraciones > 1000000) return false

            val sal = Base64.getDecoder().decode(partes[1])
            val hashOriginal = Base64.getDecoder().decode(partes[2])

            val hashCalculado = calcularHash(
                password,
                sal,
                iteraciones
            )

            MessageDigest.isEqual(hashOriginal, hashCalculado)

        } catch (e: Exception) {
            false
        }
    }

    private fun calcularHash(
        password: String,
        sal: ByteArray,
        iteraciones: Int
    ): ByteArray {
        val especificacion = PBEKeySpec(
            password.toCharArray(),
            sal,
            iteraciones,
            LONGITUD_CLAVE
        )

        return try {
            SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(especificacion)
                .encoded
        } finally {
            especificacion.clearPassword()
        }
    }
}


