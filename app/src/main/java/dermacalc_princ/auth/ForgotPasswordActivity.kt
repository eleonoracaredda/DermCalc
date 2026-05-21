package com.example.dermcalc_princ.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dermcalc_princ.R
import com.google.android.material.textfield.TextInputEditText
import com.example.dermcalc_princ.database.AppDatabase
import com.example.dermcalc_princ.dominio.User
import kotlinx.coroutines.launch
import com.example.dermcalc_princ.utils.hashPassword
import com.example.dermcalc_princ.utils.InputValidator

import com.example.dermcalc_princ.utils.LocaleHelper
import android.content.Context

// Gestisce il recupero della password smarrita tramite verifica del Codice Fiscale
class ForgotPasswordActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    // Memorizza l'utente una volta verificata l'identità
    private var verifiedUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Riferimenti ai componenti UI
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etTaxCode = findViewById<TextInputEditText>(R.id.etTaxCode)
        val etNewPassword = findViewById<TextInputEditText>(R.id.etNewPassword)
        val btnVerify = findViewById<Button>(R.id.btnVerify)
        val btnResetPassword = findViewById<Button>(R.id.btnResetPassword)
        val resetSection = findViewById<View>(R.id.resetSection)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        val database = AppDatabase.getDatabase(this)

        // Primo step: verifica che Email e Codice Fiscale corrispondano a un utente registrato
        btnVerify.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val taxCode = etTaxCode.text.toString().trim()

            if (email.isEmpty() || taxCode.isEmpty()) {
                Toast.makeText(this, "Inserisci tutti i dati", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = database.userDao().getUserByEmail(email)
                // Confronto case-insensitive per il codice fiscale per evitare errori di battitura
                if (user != null && user.taxCode.equals(taxCode, ignoreCase = true)) {
                    verifiedUser = user
                    // Mostra i campi per il reset e nasconde quelli di verifica
                    resetSection.visibility = View.VISIBLE
                    btnVerify.visibility = View.GONE
                    etEmail.isEnabled = false
                    etTaxCode.isEnabled = false
                    Toast.makeText(this@ForgotPasswordActivity, "Dati verificati", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ForgotPasswordActivity, "Dati non corrispondenti", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Secondo step: salvataggio della nuova password
        btnResetPassword.setOnClickListener {
            val newPassword = etNewPassword.text.toString().trim()

            // Verifica che la nuova password rispetti i criteri di sicurezza
            if (!InputValidator.isPasswordStrong(newPassword)) {
                etNewPassword.error = "La password deve contenere almeno 8 caratteri, numeri e lettere"
                return@setOnClickListener
            }

            verifiedUser?.let { user ->
                lifecycleScope.launch {
                    // Crea una copia dell'utente con la nuova password hashata
                    val updatedUser = user.copy(password = hashPassword(newPassword))
                    database.userDao().updateUser(updatedUser)
                    Toast.makeText(this@ForgotPasswordActivity, "Password reimpostata con successo", Toast.LENGTH_SHORT).show()
                    finish() // Torna alla schermata di login
                }
            }
        }

        // Semplice navigazione a ritroso
        tvBackToLogin.setOnClickListener {
            finish()
        }
    }
}
