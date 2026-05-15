package Dermacalc_princ

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.Dermcalc_princ.R
import com.google.android.material.textfield.TextInputEditText
import Database.AppDatabase
import Dominio.User
import kotlinx.coroutines.launch

/**
 * RegisterActivity gestisce la registrazione di un nuovo utente nel database locale.
 */
class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Riferimenti ai campi di input
        val etFirstName = findViewById<TextInputEditText>(R.id.etFirstName)
        val etLastName = findViewById<TextInputEditText>(R.id.etLastName)
        val etTaxCode = findViewById<TextInputEditText>(R.id.etTaxCode)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        // Inizializzazione del database
        val database = AppDatabase.getDatabase(this)

        // Gestione del click sul tasto Registrati
        btnRegister.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val taxCode = etTaxCode.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (firstName.isNotEmpty() && lastName.isNotEmpty() && taxCode.isNotEmpty() &&
                email.isNotEmpty() && password.isNotEmpty()
            ) {
                lifecycleScope.launch {
                    // Controllo se l'utente esiste già (tramite Codice Fiscale)
                    val existingUser = database.userDao().getUserByTaxCode(taxCode)
                    if (existingUser == null) {
                        // Creazione del nuovo utente
                        val newUser = User(
                            taxCode = taxCode,
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            password = password
                        )
                        // Inserimento nel database
                        database.userDao().insertUser(newUser)
                        Toast.makeText(this@RegisterActivity, "Registrazione completata!", Toast.LENGTH_SHORT).show()
                        
                        // Chiude l'activity e torna alla schermata di Login
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Utente già registrato con questo Codice Fiscale", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Per favore, compila tutti i campi", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
