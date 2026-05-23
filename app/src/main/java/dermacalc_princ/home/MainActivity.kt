package dermacalc_princ.home

import dermacalc_princ.auth.LoginActivity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// Punto di ingresso dell'applicazione (Launcher).
// Attualmente reindirizza immediatamente alla schermata di Login.
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Avvio immediato della LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        
        // Rimozione dalla memoria per evitare di tornare qui premendo "Indietro"
        finish()
    }
}
