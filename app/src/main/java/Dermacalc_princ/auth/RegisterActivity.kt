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
import Dermacalc_princ.pazienti.PazientiActivity
import Dominio.User
import kotlinx.coroutines.launch
import Utils.InputValidator
import Utils.SessionManager
import Utils.hashPassword


// Classe RegisterActivity: gestisce la creazione di un nuovo account utente
class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val sessionManager = SessionManager(this)

        // Associazione dei widget del layout agli oggetti Kotlin
        val etFirstName = findViewById<TextInputEditText>(R.id.etFirstName)
        val etLastName = findViewById<TextInputEditText>(R.id.etLastName)
        val etTaxCode = findViewById<TextInputEditText>(R.id.etTaxCode)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        val tvTitle = findViewById<TextView>(R.id.tvRegisterTitle)

        // Riferimento al database Room
        val database = AppDatabase.getDatabase(this)

        // Controllo se sono in modalità modifica profilo
        val isEditMode = intent.getBooleanExtra("EDIT_MODE", false)
        val currentDoctorId = sessionManager.getDoctorId()

        if (isEditMode && currentDoctorId != null) {
            tvTitle.text = "Modifica Profilo"
            btnRegister.text = "Aggiorna Profilo"
            tvLogin.visibility = android.view.View.GONE
            etTaxCode.isEnabled = false // Il codice fiscale solitamente non si cambia essendo PK

            lifecycleScope.launch {
                val user = database.userDao().getUserByTaxCode(currentDoctorId)
                user?.let {
                    etFirstName.setText(it.firstName)
                    etLastName.setText(it.lastName)
                    etTaxCode.setText(it.taxCode)
                    etEmail.setText(it.email)
                    etPassword.setText("")   // non mostrare l’hash
                }
            }
        }

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
            //NON obbligare la password in modifica profilo
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

            // Registrazione → password obbligatoria
            if (!isEditMode && !InputValidator.isPasswordStrong(password)) {
                etPassword.error = "La password deve contenere almeno 8 caratteri, numeri e lettere"
                return@setOnClickListener
            }

            // Modifica profilo → password opzionale, ma se la cambia deve essere forte
            if (isEditMode && password.isNotEmpty() && !InputValidator.isPasswordStrong(password)) {
                etPassword.error = "La nuova password deve contenere almeno 8 caratteri"
                return@setOnClickListener
            }

            // CONTROLLO EMAIL GIÀ REGISTRATA
            lifecycleScope.launch {

                if (!isEditMode) {
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
                }

                // Se sono in modifica profilo e la password è vuota → mantieni quella vecchia
                val finalPassword = if (isEditMode && password.isEmpty()) {
                    database.userDao().getUserByTaxCode(taxCode)?.password ?: hashPassword(password)
                } else {
                    // Altrimenti hash della nuova password
                    hashPassword(password)
                }

                val user = User(
                    taxCode = taxCode,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = finalPassword
                )


                database.userDao().insertUser(user)

                // Salva sessione (aggiorna nome se cambiato)
                sessionManager.saveDoctor(taxCode, firstName)

                Toast.makeText(
                    this@RegisterActivity,
                    if (isEditMode) "Profilo aggiornato!" else "Registrazione completata!",
                    Toast.LENGTH_SHORT
                ).show()

                if (isEditMode) {
                    finish()
                } else {
                    // Vai alla lista pazienti
                    val intent = Intent(this@RegisterActivity, PazientiActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
