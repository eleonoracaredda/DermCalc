package dermcalc_princ.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.dermcalc_princ.R
import database.AppDatabase
import dermcalc_princ.auth.LoginActivity
import dermcalc_princ.calcolatori.BmiActivity
import dermcalc_princ.calcolatori.EasiActivity
import dermcalc_princ.calcolatori.PasiActivity
import utils.SessionManager
import kotlinx.coroutines.launch

// Classe HomeActivity: Menu principale dell'app dove l'utente sceglie quale calcolatore utilizzare
class HomeActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Carica il layout grafico della Home
        setContentView(R.layout.activity_home)

        val sessionManager = SessionManager(this)
        
        // Riferimenti ai componenti presenti nel layout
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val cardBmi = findViewById<MaterialCardView>(R.id.cardBmi)
        val cardPasi = findViewById<MaterialCardView>(R.id.cardPasi)
        val cardEasi = findViewById<MaterialCardView>(R.id.cardEasi)
        val cardStorico = findViewById<MaterialCardView>(R.id.cardStorico)
        val btnBackToList = findViewById<Button>(R.id.btnBackToList)
        val tvSelectedPaziente = findViewById<TextView>(R.id.tvSelectedPaziente)
        val tvPazienteDetails = findViewById<TextView>(R.id.tvPazienteDetails)
        val tvDoctorHeaderName = findViewById<TextView>(R.id.tvDoctorHeaderName)
        val fabAddPatient = findViewById<FloatingActionButton>(R.id.fabAddPatient)

        // Mostra il nome del medico nell'intestazione
        tvDoctorHeaderName.text = "Dott. ${sessionManager.getDoctorName() ?: ""}"

        // Listener per aggiungere un nuovo paziente (shortcut dalla Home)
        fabAddPatient.setOnClickListener {
            val intent = Intent(this, dermcalc_princ.pazienti.CreatePazienteActivity::class.java)
            startActivity(intent)
        }

        // Recupero ID paziente passato da PazientiActivity
        val pazienteId = intent.getIntExtra("PAZIENTE_ID", -1)
        // Sicurezza: se manca l’ID, chiudo
        if (pazienteId != -1) {
            val database = AppDatabase.getDatabase(this)
            lifecycleScope.launch {
                val paziente = database.pazienteDao().getById(pazienteId)
                if (paziente != null) {
                    tvSelectedPaziente.text = "Paziente: ${paziente.nome} ${paziente.cognome}"
                    val sessoEsteso = if (paziente.sesso == "M") "Maschio" else "Femmina"
                    val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.ITALY)
                    tvPazienteDetails.text = "Sesso: $sessoEsteso | Data di nascita: ${dateFormat.format(paziente.dataNascita)}"
                }
            }
        }

        // Gestione del pulsante per tornare alla lista pazienti
        btnBackToList.setOnClickListener {
            finish() // Semplicemente chiude la HomeActivity per tornare alla lista (PazientiActivity)
        }

        // Gestione del pulsante Storico
        cardStorico.setOnClickListener {
            val intent = Intent(this, StoricoActivity::class.java)
            intent.putExtra("PAZIENTE_ID", pazienteId)
            startActivity(intent)
        }

        // Gestione del pulsante Logout per uscire dall'account
        btnLogout.setOnClickListener {
            sessionManager.logout()
            val intent = Intent(this, LoginActivity::class.java)
            
            // FLAG_ACTIVITY_CLEAR_TASK assicura che lo stack delle attività venga svuotato,
            // impedendo di tornare alla Home con il tasto indietro dopo essere usciti.
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            
            startActivity(intent)
            finish()
        }

        // Passaggio dell'ID paziente selezionato alle varie attività dei calcolatori
        // Questo permette di salvare le misurazioni associandole correttamente al paziente.

        // Listener per avviare il calcolatore dell'Indice di Massa Corporea (BMI)
        cardBmi.setOnClickListener {
            val intent = Intent(this, BmiActivity::class.java)
            intent.putExtra("PAZIENTE_ID", pazienteId)
            startActivity(intent)
        }

        // Listener per avviare il calcolatore PASI (Psoriasi)
        cardPasi.setOnClickListener {
            val intent = Intent(this, PasiActivity::class.java)
            intent.putExtra("PAZIENTE_ID", pazienteId)
            startActivity(intent)
        }

        // Listener per avviare il calcolatore EASI (Eczema)
        cardEasi.setOnClickListener {
            val intent = Intent(this, EasiActivity::class.java)
            intent.putExtra("PAZIENTE_ID", pazienteId)
            startActivity(intent)
        }
    }
}
