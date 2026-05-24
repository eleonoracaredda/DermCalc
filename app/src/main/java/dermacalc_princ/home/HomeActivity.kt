package dermacalc_princ.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dermcalc_princ.R
import Database.AppDatabase
import dermacalc_princ.auth.LoginActivity
import dermacalc_princ.calcolatori.BmiActivity
import dermacalc_princ.calcolatori.EasiActivity
import dermacalc_princ.calcolatori.PasiActivity
import dermacalc_princ.pazienti.CreatePazienteActivity
import Utils.SessionManager
import kotlinx.coroutines.launch
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import Utils.LocaleHelper
import android.content.Context
import dermacalc_princ.misurazioni.MisurazioniListActivity
import java.text.SimpleDateFormat
import java.util.Locale


// Classe HomeActivity: Dashboard principale dell'app per il paziente selezionato
class HomeActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Carica il layout modernizzato della Home
        setContentView(R.layout.activity_home)

        val sessionManager = SessionManager(this)
        
        // Header Utente: Benvenuto Dottore
        val tvDoctorHeaderName = findViewById<TextView>(R.id.tvDoctorHeaderName)
        val doctorName = sessionManager.getDoctorName()
        if (doctorName != null) {
            tvDoctorHeaderName.text = "Dott. $doctorName"
        }
        
        // Riferimenti alle Card per il layout a griglia
        val cardBmi = findViewById<MaterialCardView>(R.id.cardBmi)
        val cardPasi = findViewById<MaterialCardView>(R.id.cardPasi)
        val cardEasi = findViewById<MaterialCardView>(R.id.cardEasi)
        val cardStorico = findViewById<MaterialCardView>(R.id.cardStorico)
        val cardMisurazioniList = findViewById<MaterialCardView>(R.id.cardMisurazioniList)
        
        // Riferimenti ad altri elementi UI
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnBackToList = findViewById<Button>(R.id.btnBackToList)
        val fabEditPatient = findViewById<FloatingActionButton>(R.id.fabEditPatient)
        val tvSelectedPaziente = findViewById<TextView>(R.id.tvSelectedPaziente)
        val tvPazienteDetails = findViewById<TextView>(R.id.tvPazienteDetails)

        // Recupero ID paziente passato da PazientiActivity
        val pazienteId = intent.getIntExtra("PAZIENTE_ID", -1)
        
        // Caricamento dati del paziente se presente
        if (pazienteId != -1) {
            val database = AppDatabase.getDatabase(this)
            lifecycleScope.launch {
                val paziente = database.pazienteDao().getById(pazienteId)
                if (paziente != null) {
                    tvSelectedPaziente.text = "${paziente.nome} ${paziente.cognome}"
                    val sessoEsteso = if (paziente.sesso == "M") "Maschio" else "Femmina"
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
                    tvPazienteDetails.text = "Sesso: $sessoEsteso | Data di nascita: ${dateFormat.format(paziente.dataNascita)}"
                }
            }
        }

        // Floating Action Button per modificare il paziente selezionato
        fabEditPatient.setOnClickListener {
            if (pazienteId != -1) {
                val intent = Intent(this, CreatePazienteActivity::class.java)
                intent.putExtra("PAZIENTE_ID", pazienteId)
                startActivity(intent)
            }
        }

        // Torna alla lista pazienti
        btnBackToList.setOnClickListener {
            finish()
        }

        // Gestione Logout
        btnLogout.setOnClickListener {
            sessionManager.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Click listeners per le card dei calcolatori
        cardBmi.setOnClickListener {
            val intent = Intent(this, BmiActivity::class.java)
            intent.putExtra("PAZIENTE_ID", pazienteId)
            startActivity(intent)
        }

        cardPasi.setOnClickListener {
            val intent = Intent(this, PasiActivity::class.java)
            intent.putExtra("PAZIENTE_ID", pazienteId)
            startActivity(intent)
        }

        cardEasi.setOnClickListener {
            val intent = Intent(this, EasiActivity::class.java)
            intent.putExtra("PAZIENTE_ID", pazienteId)
            startActivity(intent)
        }

        cardStorico.setOnClickListener {
            val intent = Intent(this, StoricoActivity::class.java)
            intent.putExtra("PAZIENTE_ID", pazienteId)
            startActivity(intent)
        }

        cardMisurazioniList.setOnClickListener {
            val intent = Intent(this, MisurazioniListActivity::class.java)
            intent.putExtra("PAZIENTE_ID", pazienteId)
            startActivity(intent)
        }
    }
}
