package Dermacalc_princ

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
import kotlinx.coroutines.launch

/**
 * MainActivity gestisce la schermata di Login dell'applicazione collegata al database locale.
 */
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        // Riferimenti ai campi di input
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtRegister = findViewById<TextView>(R.id.txtRegister)

        // Inizializzazione del database
        val database = AppDatabase.getDatabase(this)
        
        // Gestione del click sul tasto Login
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Controllo delle credenziali nel database
                lifecycleScope.launch {
                    val user = database.userDao().login(email, password)
                    
                    if (user != null) {
                        // Utente trovato: Login effettuato con successo
                        Toast.makeText(this@MainActivity, "Bentornato ${user.firstName}!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Utente non trovato o password errata
                        Toast.makeText(this@MainActivity, "Email o password errate", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Inserisci email e password", Toast.LENGTH_SHORT).show()
            }
        }

        // Gestione del click per andare alla registrazione
        txtRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
