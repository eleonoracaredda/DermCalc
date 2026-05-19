package Utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("DermCalcPrefs", Context.MODE_PRIVATE)
    companion object {
        private const val KEY_DOCTOR_ID = "doctor_id"
        private const val KEY_DOCTOR_NAME = "doctor_name"
    }
    fun saveDoctor(taxCode: String, name: String) {
        prefs.edit().apply {
            putString(KEY_DOCTOR_ID, taxCode)
            putString(KEY_DOCTOR_NAME, name)
            apply()
        }
    }
    fun getDoctorId(): String? {
        return prefs.getString(KEY_DOCTOR_ID, null)
    }

    fun getDoctorName(): String? {
        return prefs.getString(KEY_DOCTOR_NAME, null)
    }
    fun logout() {
        prefs.edit().clear().apply()
    }
}
