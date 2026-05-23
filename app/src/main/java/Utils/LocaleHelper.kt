package Utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale


// Utility +per gestire il cambio lingua dinamico nell'applicazione.
object LocaleHelper {

    // Applica la lingua salvata al contesto dell'Activity.
    fun applyLocale(context: Context): Context {
        val sessionManager = SessionManager(context)
        val lang = sessionManager.getLanguage()
        return updateResources(context, lang)
    }


    //Aggiorna la configurazione delle risorse con la nuova lingua.
    @Suppress("DEPRECATION")
    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val res = context.resources
        val config = Configuration(res.configuration)
        
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
