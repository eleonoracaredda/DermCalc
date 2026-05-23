package Utils

import android.util.Patterns

// Utility per la validazione formale degli input utente nelle diverse schermate
object InputValidator {

    // Verifica se l'indirizzo email inserito segue il formato standard (es. nome@dominio.it)
    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Requisiti minimi di sicurezza per la password: deve avere almeno 8 caratteri
    fun isPasswordStrong(password: String): Boolean {
        return password.length >= 8
    }

    // Assicura che i nomi inseriti (paziente o medico) non siano stringhe vuote o solo spazi
    fun isNameValid(name: String): Boolean {
        return name.isNotBlank()
    }

    // Verifica la lunghezza del Codice Fiscale (deve essere esattamente di 16 caratteri)
    fun isCodiceFiscaleValid(cf: String): Boolean {
        return cf.length == 16
    }

    // Funzione helper che verifica se tutti i campi passati come argomenti sono popolati
    fun isNotEmpty(vararg fields: String): Boolean {
        return fields.all { it.isNotBlank() }
    }
}
