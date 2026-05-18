package Utils

object InputValidator {

    // Email valida
    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Password valida (minimo 8 caratteri)
    fun isPasswordStrong(password: String): Boolean {
        return password.length >= 8
    }

    // Nome e cognome non vuoti
    fun isNameValid(name: String): Boolean {
        return name.isNotBlank()
    }

    // Codice fiscale valido (16 caratteri)
    fun isCodiceFiscaleValid(cf: String): Boolean {
        return cf.length == 16
    }

    // Controllo campi vuoti
    fun isNotEmpty(vararg fields: String): Boolean {
        return fields.all { it.isNotBlank() }
    }
}
