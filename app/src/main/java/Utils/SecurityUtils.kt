package com.example.dermcalc_princ.utils

import java.security.MessageDigest

// Funzione di utilità per trasformare una stringa (password) in un hash sicuro SHA-256.
// Le password non vengono mai salvate in chiaro nel database locale.
fun hashPassword(password: String): String {
    // Calcola l'hash dei byte della password
    val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
    // Converte l'array di byte in una stringa esadecimale leggibile
    return bytes.joinToString("") { "%02x".format(it) }
}
