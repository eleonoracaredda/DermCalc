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

            // Verifica che tutti i campi siano stati compilati
            if (firstName.isNotEmpty() && lastName.isNotEmpty() && taxCode.isNotEmpty() &&
                email.isNotEmpty() && password.isNotEmpty()
            ) {
                lifecycleScope.launch {
                    // Verifica se esiste già un utente con lo stesso codice fiscale (chiave primaria)
                    val existingUser = database.userDao().getUserByTaxCode(taxCode)
                    if (existingUser == null) {
                        // Creazione dell'oggetto User con i dati inseriti
                        val newUser = User(
                            taxCode = taxCode,
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            password = password
                        )
                        // Salvataggio permanente nel database
                        database.userDao().insertUser(newUser)
                        Toast.makeText(this@RegisterActivity, "Registrazione completata!", Toast.LENGTH_SHORT).show()
                        
                        // Avvio della HomeActivity dopo la registrazione avvenuta con successo
                        val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                        // Rimuove le attività precedenti dallo stack (per sicurezza e pulizia)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        // Messaggio se il codice fiscale è già presente a sistema
                        Toast.makeText(this@RegisterActivity, "Utente già registrato con questo Codice Fiscale", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Notifica all'utente se mancano dei campi obbligatori
                Toast.makeText(this, "Per favore, compila tutti i campi", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
