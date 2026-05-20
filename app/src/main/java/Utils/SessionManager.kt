package com.example.dermcalc_princ.utils

import android.content.Context
import android.content.SharedPreferences

// Gestisce la persistenza locale della sessione dell'utente (medico loggato).
// Utilizza le SharedPreferences per mantenere l'accesso anche dopo la chiusura dell'app.
class SessionManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("DermCalcPrefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_DOCTOR_ID = "doctor_id"
        private const val KEY_DOCTOR_NAME = "doctor_name"
    }
    
    // Salva i dati del medico quando effettua il login o si registra
    fun saveDoctor(taxCode: String, name: String) {
        prefs.edit().apply {
            putString(KEY_DOCTOR_ID, taxCode)
            putString(KEY_DOCTOR_NAME, name)
            apply()
        }
    }
    
    // Recupera l'identificativo univoco (Codice Fiscale) del medico corrente
    fun getDoctorId(): String? {
        return prefs.getString(KEY_DOCTOR_ID, null)
    }

    // Recupera il nome del medico per scopi di visualizzazione (es. "Bentornato Nome")
    fun getDoctorName(): String? {
        return prefs.getString(KEY_DOCTOR_NAME, null)
    }
    
    // Cancella tutti i dati di sessione (Logout)
    fun logout() {
        prefs.edit().clear().apply()
    }
}
