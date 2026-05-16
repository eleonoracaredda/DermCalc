package Dermacalc_princ

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// Classe MainActivity: punto di ingresso dell'applicazione che gestisce il reindirizzamento iniziale
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Crea un Intent per avviare la schermata di login
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        
        // Chiude la MainActivity per rimuoverla dallo stack delle attività,
        // evitando che l'utente ci torni premendo il tasto "indietro" dal login
        finish()
    }
}
