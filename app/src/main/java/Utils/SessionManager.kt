package Utils

import android.content.Context
import android.content.SharedPreferences

// Gestisce la sessione del medico loggato usando le SharedPreferences
class SessionManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("DermCalcPrefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_DOCTOR_ID = "doctor_id"
        private const val KEY_DOCTOR_NAME = "doctor_name"
        private const val KEY_LANGUAGE = "selected_language"
    }
    
    // Salva la lingua preferita
    fun saveLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
    }

    // Recupera la lingua salvata (default italiano)
    fun getLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, "it") ?: "it"
    }
    
    // Salva i dati del medico al login o registrazione
    fun saveDoctor(taxCode: String, name: String) {
        prefs.edit().apply {
            putString(KEY_DOCTOR_ID, taxCode)
            putString(KEY_DOCTOR_NAME, name)
            apply()
        }
    }
    
    // Recupera il Codice Fiscale del medico corrente
    fun getDoctorId(): String? {
        return prefs.getString(KEY_DOCTOR_ID, null)
    }

    // Recupera il nome del medico per la UI
    fun getDoctorName(): String? {
        return prefs.getString(KEY_DOCTOR_NAME, null)
    }
    
    // Cancella i dati di sessione (Logout)
    fun logout() {
        prefs.edit().clear().apply()
    }
}
