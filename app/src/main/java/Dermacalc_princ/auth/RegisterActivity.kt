package Dermacalc_princ.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.Dermcalc_princ.R
import com.google.android.material.textfield.TextInputEditText
import Database.AppDatabase
import Dermacalc_princ.home.HomeActivity
import Dominio.User
import kotlinx.coroutines.launch
import Utils.InputValidator

// Classe RegisterActivity: gestisce la creazione di un nuovo account utente
class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Associazione dei widget del layout agli oggetti Kotlin
        val etFirstName = findViewById<TextInputEditText>(R.id.etFirstName)
        val etLastName = findViewById<TextInputEditText>(R.id.etLastName)
        val etTaxCode = findViewById<TextInputEditText>(R.id.etTaxCode)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        // Riferimento al database Room
        val database = AppDatabase.getDatabase(this)

        // Listener per tornare alla schermata di login se l'utente ha già un account
        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Gestione del processo di registrazione alla pressione del pulsante
        btnRegister.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val taxCode = etTaxCode.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // VALIDAZIONE COMPLETA
            if (!InputValidator.isNotEmpty(firstName, lastName, taxCode, email, password)) {
                Toast.makeText(this, "Compila tutti i campi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!InputValidator.isNameValid(firstName)) {
                etFirstName.error = "Nome non valido"
                return@setOnClickListener
            }

            if (!InputValidator.isNameValid(lastName)) {
                etLastName.error = "Cognome non valido"
                return@setOnClickListener
            }

            if (!InputValidator.isCodiceFiscaleValid(taxCode)) {
                etTaxCode.error = "Codice fiscale non valido"
                return@setOnClickListener
            }

            if (!InputValidator.isEmailValid(email)) {
                etEmail.error = "Email non valida"
                return@setOnClickListener
            }

            if (!InputValidator.isPasswordStrong(password)) {
                etPassword.error = "La password deve contenere almeno 8 caratteri, numeri e lettere"
                return@setOnClickListener
            }

            // CONTROLLO EMAIL GIÀ REGISTRATA
            lifecycleScope.launch {

                val existingEmail = database.userDao().getUserByEmail(email)
                if (existingEmail != null) {
                    etEmail.error = "Email già registrata"
                    return@launch
                }

                // CONTROLLO CODICE FISCALE GIÀ REGISTRATO
                val existingUser = database.userDao().getUserByTaxCode(taxCode)
                if (existingUser != null) {
                    etTaxCode.error = "Codice fiscale già registrato"
                    return@launch
                }

                // CREAZIONE UTENTE
                val newUser = User(
                    taxCode = taxCode,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = password
                )

                database.userDao().insertUser(newUser)

                Toast.makeText(
                    this@RegisterActivity,
                    "Registrazione completata!",
                    Toast.LENGTH_SHORT
                ).show()

                // Vai alla Home
                val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}
