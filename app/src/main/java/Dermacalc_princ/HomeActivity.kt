package Dermacalc_princ

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.Dermcalc_princ.R

// Classe HomeActivity: Menu principale dell'app dove l'utente sceglie quale calcolatore utilizzare
class HomeActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Carica il layout grafico della Home
        setContentView(R.layout.activity_home)
        
        // Riferimenti ai pulsanti presenti nel layout
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnBmi = findViewById<Button>(R.id.btnBmi)
        val btnPasi = findViewById<Button>(R.id.btnPasi)
        val btnEasi = findViewById<Button>(R.id.btnEasi)

        // Gestione del pulsante Logout per uscire dall'account
        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            
            // FLAG_ACTIVITY_CLEAR_TASK assicura che lo stack delle attività venga svuotato,
            // impedendo di tornare alla Home con il tasto indietro dopo essere usciti.
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            
            startActivity(intent)
            finish()
        }
        
        // Listener per avviare il calcolatore dell'Indice di Massa Corporea (BMI)
        btnBmi.setOnClickListener {
            val intent = Intent(this, BmiActivity::class.java)
            startActivity(intent)
        }

        // Listener per avviare il calcolatore PASI (Psoriasi)
        btnPasi.setOnClickListener {
            val intent = Intent(this, PasiActivity::class.java)
            startActivity(intent)
        }

        // Listener per avviare il calcolatore EASI (Eczema)
        btnEasi.setOnClickListener {
            val intent = Intent(this, EasiActivity::class.java)
            startActivity(intent)
        }
    }
}
