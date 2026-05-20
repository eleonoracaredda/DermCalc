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
import utils.InputValidator
import utils.SessionManager
import utils.hashPassword


// Gestisce l'autenticazione del medico tramite Email e Password
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Forza il tema chiaro per coerenza grafica (Dark Mode disabilitata)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        // Gestore della sessione locale (SharedPreferences)
        val sessionManager = SessionManager(this)

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
                etEmail.error = "Inserisci email e password"
                return@setOnClickListener
            }

            if (!InputValidator.isEmailValid(email)) {
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
