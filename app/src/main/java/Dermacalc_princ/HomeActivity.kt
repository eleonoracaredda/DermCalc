package Dermacalc_princ

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.Dermcalc_princ.R
import Database.AppDatabase
import kotlinx.coroutines.launch

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
        val btnBackToList = findViewById<Button>(R.id.btnBackToList)
        val tvSelectedPaziente = findViewById<TextView>(R.id.tvSelectedPaziente)

        // Recupero ID paziente passato da PazientiActivity
        val pazienteId = intent.getIntExtra("PAZIENTE_ID", -1)
        // Sicurezza: se manca l’ID, chiudo
        if (pazienteId != -1) {
            val database = AppDatabase.getDatabase(this)
            lifecycleScope.launch {
                val paziente = database.pazienteDao().getById(pazienteId)
                if (paziente != null) {
                    tvSelectedPaziente.text = "Paziente: ${paziente.nome} ${paziente.cognome}"
                }
            }
        }

        // Gestione del pulsante per tornare alla lista pazienti
        btnBackToList.setOnClickListener {
            finish() // Semplicemente chiude la HomeActivity per tornare alla lista (PazientiActivity)
        }

        // Gestione del pulsante Logout per uscire dall'account
        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            
            // FLAG_ACTIVITY_CLEAR_TASK assicura che lo stack delle attività venga svuotato,
            // impedendo di tornare alla Home con il tasto indietro dopo essere usciti.
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            
            startActivity(intent)
            finish()
        }

        //Passaggio ID alle activity
        // Listener per avviare il calcolatore dell'Indice di Massa Corporea (BMI)
        btnBmi.setOnClickListener {
            val intent = Intent(this, BmiActivity::class.java)
            intent.putExtra("PAZIENTE_ID", pazienteId)
            startActivity(intent)
        }

        // Listener per avviare il calcolatore PASI (Psoriasi)
        btnPasi.setOnClickListener {
            val intent = Intent(this, PasiActivity::class.java)
            intent.putExtra("PAZIENTE_ID", pazienteId)
            startActivity(intent)
        }

        // Listener per avviare il calcolatore EASI (Eczema)
        btnEasi.setOnClickListener {
            val intent = Intent(this, EasiActivity::class.java)
            intent.putExtra("PAZIENTE_ID", pazienteId)
            startActivity(intent)
        }
    }
}
