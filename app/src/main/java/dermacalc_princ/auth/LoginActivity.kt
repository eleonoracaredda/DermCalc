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
import Database.AppDatabase
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

// Gestisce l'autenticazione del medico tramite Email e Password
class LoginActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Applica la lingua salvata prima di creare l'Activity
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    // FUNZIONE PER CAMBIARE LINGUA ---
    private fun setLocale(lang: String) {
        val sessionManager = SessionManager(this)
        val currentLang = sessionManager.getLanguage()
        if (currentLang == lang) return // Evita loop infiniti se la lingua è già corretta

        sessionManager.saveLanguage(lang)
        recreate()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        // Forza il tema chiaro prima della creazione della view
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Gestore della sessione locale (SharedPreferences)
        val sessionManager = SessionManager(this)

        // COLLEGAMENTO SPINNER LINGUA
        val spLanguage = findViewById<Spinner>(R.id.spLanguage)
        
        // Imposta la posizione dello spinner in base alla lingua salvata
        val savedLang = sessionManager.getLanguage()
        if (savedLang == "en") spLanguage.setSelection(1) else spLanguage.setSelection(0)

        spLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLang = if (position == 1) "en" else "it"
                setLocale(selectedLang)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Inizializzazione view
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtForgotPassword = findViewById<TextView>(R.id.txtForgotPassword)
        val txtRegister = findViewById<TextView>(R.id.txtRegister)

        val database = AppDatabase.getDatabase(this)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validazione input prima di interrogare il DB
            if (!InputValidator.isNotEmpty(email, password)) {
                Toast.makeText(this, "Inserisci email e password", Toast.LENGTH_SHORT).show()
                etEmail.error = "Campo obbligatorio"
                return@setOnClickListener
            }

            if (!InputValidator.isEmailValid(email)) {
                Toast.makeText(this, "Formato email non valido", Toast.LENGTH_SHORT).show()
                etEmail.error = "Formato email non valido"
                return@setOnClickListener
            }

            // Tentativo di login
            lifecycleScope.launch {
                val user = database.userDao().getUserByEmail(email)

                // Confronto tra l'hash della password inserita e quello nel database
                if (user != null && user.password == hashPassword(password)) {
                    // Salvataggio medico corrente nella sessione
                    sessionManager.saveDoctor(user.taxCode, user.firstName)

                    Toast.makeText(
                        this@LoginActivity,
                        "Bentornat* Dott. ${user.firstName}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigazione alla dashboard principale (Lista Pazienti)
                    startActivity(Intent(this@LoginActivity, PazientiActivity::class.java))
                    finish()
                } else {
                    etPassword.error = "Credenziali errate"
                }
            }
        }

        // Navigazione al recupero password
        txtForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Navigazione alla registrazione
        txtRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
