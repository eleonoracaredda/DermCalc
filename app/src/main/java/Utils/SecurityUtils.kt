package Utils

import java.security.MessageDigest

// Trasforma una stringa (password) in un hash sicuro SHA-256
fun hashPassword(password: String): String {
    // Calcola l'hash dei byte
    val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())

    // Converte l'array di byte in stringa esadecimale
    return bytes.joinToString("") { "%02x".format(it) }
}
