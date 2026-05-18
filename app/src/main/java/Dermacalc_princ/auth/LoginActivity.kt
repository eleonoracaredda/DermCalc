package Dermacalc_princ.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.Dermcalc_princ.R
import com.google.android.material.textfield.TextInputEditText
import Database.AppDatabase
import Dermacalc_princ.pazienti.PazientiActivity
import kotlinx.coroutines.launch
import Utils.InputValidator

// Classe LoginActivity: gestisce l'interfaccia di accesso per l'utente
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Forza l'applicazione a non usare il tema scuro per mantenere l'estetica desiderata
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        // Collegamento degli elementi dell'interfaccia grafica tramite ID
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtRegister = findViewById<TextView>(R.id.txtRegister)

        // Inizializzazione del database per la verifica delle credenziali
        val database = AppDatabase.getDatabase(this)

        // Listener per il pulsante di login
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // VALIDAZIONE COMPLETA PRIMA DEL LOGIN

            // Campi vuoti
            if (!InputValidator.isNotEmpty(email, password)) {
                etEmail.error = "Inserisci email e password"
                return@setOnClickListener
            }

            // Email valida
            if (!InputValidator.isEmailValid(email)) {
                etEmail.error = "Formato email non valido"
                return@setOnClickListener
            }

            // Password forte
            if (!InputValidator.isPasswordStrong(password)) {
                etPassword.error = "La password deve contenere almeno 8 caratteri, numeri e lettere"
                return@setOnClickListener
            }
            // Login
            lifecycleScope.launch {
                val user = database.userDao().login(email, password)

                if (user != null) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Bentornato \${user.firstName}",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@LoginActivity, PazientiActivity::class.java))
                    finish()
                } else {
                    etPassword.error = "Credenziali errate"
                }
            }
            // Listener per il testo "Registrati", avvia l'attività di registrazione
            txtRegister.setOnClickListener {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }
        }
    }
}