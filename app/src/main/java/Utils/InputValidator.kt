package Utils

import android.util.Patterns

// Utility per validare i dati inseriti nelle varie schermate
object InputValidator {

    // Controlla se l'email ha un formato valido (es. test@esempio.com)
    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Verifica se la password è abbastanza lunga (almeno 8 caratteri)
    fun isPasswordStrong(password: String): Boolean {
        return password.length >= 8
    }

    // Controlla che il nome non sia vuoto
    fun isNameValid(name: String): Boolean {
        return name.isNotBlank()
    }

    // Verifica la lunghezza standard del Codice Fiscale
    fun isCodiceFiscaleValid(cf: String): Boolean {
        return cf.length == 16
    }

    // Helper per controllare se più campi sono tutti popolati
    fun isNotEmpty(vararg fields: String): Boolean {
        return fields.all { it.isNotBlank() }
    }
}
