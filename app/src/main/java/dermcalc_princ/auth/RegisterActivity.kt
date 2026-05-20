package dermcalc_princ.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dermcalc_princ.R
import com.google.android.material.textfield.TextInputEditText
import database.AppDatabase
import dermcalc_princ.pazienti.PazientiActivity
import dominio.User
import kotlinx.coroutines.launch
import utils.InputValidator
import utils.SessionManager
import utils.hashPassword


// Gestisce sia la registrazione di un nuovo medico che la modifica del profilo esistente
class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val sessionManager = SessionManager(this)
        val database = AppDatabase.getDatabase(this)

        // Binding dei componenti
        val etFirstName = findViewById<TextInputEditText>(R.id.etFirstName)
        val etLastName = findViewById<TextInputEditText>(R.id.etLastName)
        val etTaxCode = findViewById<TextInputEditText>(R.id.etTaxCode)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        val tvTitle = findViewById<TextView>(R.id.tvRegisterTitle)

        // Logica per riutilizzare la schermata in modalità "Modifica Profilo"
        val isEditMode = intent.getBooleanExtra("EDIT_MODE", false)
        val currentDoctorId = sessionManager.getDoctorId()

        if (isEditMode && currentDoctorId != null) {
            tvTitle.text = "Modifica Profilo"
            btnRegister.text = "Aggiorna Profilo"
            tvLogin.visibility = android.view.View.GONE
            etTaxCode.isEnabled = false // Il Codice Fiscale è chiave primaria e non può essere cambiato

            // Caricamento dati attuali dal DB
            lifecycleScope.launch {
                val user = database.userDao().getUserByTaxCode(currentDoctorId)
                user?.let {
                    etFirstName.setText(it.firstName)
                    etLastName.setText(it.lastName)
                    etTaxCode.setText(it.taxCode)
                    etEmail.setText(it.email)
                    etPassword.setText("") // La password non viene mostrata per sicurezza
                }
            }
        }

        tvLogin.setOnClickListener {
            finish()
        }

        btnRegister.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val taxCode = etTaxCode.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validazioni granulari per garantire la qualità dei dati
            if (!isEditMode && !InputValidator.isNotEmpty(firstName, lastName, taxCode, email, password)) {
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

            // Password obbligatoria solo in registrazione
            if (!isEditMode && !InputValidator.isPasswordStrong(password)) {
                etPassword.error = "La password deve contenere almeno 8 caratteri, numeri e lettere"
                return@setOnClickListener
            }

            // In modifica profilo, la password è opzionale (si cambia solo se inserita)
            if (isEditMode && password.isNotEmpty() && !InputValidator.isPasswordStrong(password)) {
                etPassword.error = "La nuova password deve contenere almeno 8 caratteri"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                // Controlli di unicità eseguiti solo durante la nuova registrazione
                if (!isEditMode) {
                    if (database.userDao().getUserByEmail(email) != null) {
                        etEmail.error = "Email già registrata"
                        return@launch
                    }
                    if (database.userDao().getUserByTaxCode(taxCode) != null) {
                        etTaxCode.error = "Codice fiscale già registrato"
                        return@launch
                    }
                }

                // Determinazione della password finale (nuova o esistente)
                val finalPassword = if (isEditMode && password.isEmpty()) {
                    database.userDao().getUserByTaxCode(taxCode)?.password ?: hashPassword(password)
                } else {
                    hashPassword(password)
                }

                val user = User(taxCode, firstName, lastName, email, finalPassword)
                database.userDao().insertUser(user)

                // Aggiornamento sessione e feedback
                sessionManager.saveDoctor(taxCode, firstName)
                Toast.makeText(this@RegisterActivity, 
                    if (isEditMode) "Profilo aggiornato!" else "Registrazione completata!", 
                    Toast.LENGTH_SHORT).show()

                if (isEditMode) {
                    finish()
                } else {
                    val intent = Intent(this@RegisterActivity, PazientiActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
