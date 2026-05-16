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

            // Controllo che i campi non siano vuoti
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Esecuzione della query di login in una coroutine (thread in background)
                lifecycleScope.launch {
                    val user = database.userDao().login(email, password)
                    
                    if (user != null) {
                        // Se l'utente è trovato, mostra un messaggio di successo e passa alla Lista Pazienti
                        Toast.makeText(this@LoginActivity, "Accesso eseguito: ${user.firstName}", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, PazientiActivity::class.java)
                        startActivity(intent)
                        finish() // Chiude il login per non tornare indietro con il tasto back
                    } else {
                        // Se le credenziali sono errate, mostra un errore
                        Toast.makeText(this@LoginActivity, "Credenziali non valide", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Avviso se i campi sono incompleti
                Toast.makeText(this, "Inserisci tutti i dati richiesti", Toast.LENGTH_SHORT).show()
            }
        }

        // Listener per il testo "Registrati", avvia l'attività di registrazione
        txtRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
