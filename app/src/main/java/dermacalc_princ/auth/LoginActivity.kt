package dermacalc_princ.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.dermcalc_princ.R
import com.google.android.material.textfield.TextInputEditText
import database.AppDatabase
import dermacalc_princ.pazienti.PazientiActivity
import kotlinx.coroutines.launch
import Utils.InputValidator
import Utils.SessionManager
import Utils.hashPassword
import android.widget.AdapterView
import android.widget.Spinner
import android.view.View
import Utils.LocaleHelper
import android.content.Context

// Activity principale per l'autenticazione del medico e gestione lingua
class LoginActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Applica la lingua scelta prima di creare il contesto
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    // Salva la lingua nelle preferenze e ricarica l'Activity per applicare le modifiche
    private fun setLocale(lang: String) {
        val sessionManager = SessionManager(this)
        val currentLang = sessionManager.getLanguage()
        
        if (currentLang == lang) return

        sessionManager.saveLanguage(lang)
        recreate() // Ricarica l'activity con la nuova lingua
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Disabilita la modalità notte per coerenza stilistica dell'interfaccia
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inizializzazione sessione e UI
        val sessionManager = SessionManager(this)
        val spLanguage = findViewById<Spinner>(R.id.spLanguage)
        
        // Impostiamo lo spinner sulla lingua attualmente salvata (0: IT, 1: EN)
        val savedLang = sessionManager.getLanguage()
        if (savedLang == "en") spLanguage.setSelection(1) else spLanguage.setSelection(0)

        spLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLang = if (position == 1) "en" else "it"
                setLocale(selectedLang)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Riferimenti ai componenti grafici
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtForgotPassword = findViewById<TextView>(R.id.txtForgotPassword)
        val txtRegister = findViewById<TextView>(R.id.txtRegister)

        val database = AppDatabase.getDatabase(this)

        // Logica per il pulsante di Login
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Controllo che i campi non siano vuoti
            if (!InputValidator.isNotEmpty(email, password)) {
                Toast.makeText(this, getString(R.string.inserisci_email_password), Toast.LENGTH_SHORT).show()
                etEmail.error = getString(R.string.campo_obbligatorio)
                return@setOnClickListener
            }

            // Validazione del formato email
            if (!InputValidator.isEmailValid(email)) {
                Toast.makeText(this, getString(R.string.formato_email_non_valido), Toast.LENGTH_SHORT).show()
                etEmail.error = getString(R.string.formato_email_non_valido)
                return@setOnClickListener
            }

            // Verifica credenziali nel database
            lifecycleScope.launch {
                val user = database.userDao().getUserByEmail(email)

                // Se l'utente esiste, verifichiamo che l'hash della password coincida
                if (user != null && user.password == hashPassword(password)) {
                    // Salviamo i dati dell'utente nella sessione
                    sessionManager.saveDoctor(user.taxCode, user.firstName)

                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.bentornato_dott, user.firstName),
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(Intent(this@LoginActivity, PazientiActivity::class.java))
                    finish()
                } else {
                    etPassword.error = getString(R.string.credenziali_errate)
                }
            }
        }

        // Navigazione alla schermata di recupero password
        txtForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Navigazione alla schermata di registrazione
        txtRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
