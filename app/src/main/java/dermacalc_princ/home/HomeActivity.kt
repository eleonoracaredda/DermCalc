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


// Dashboard principale per il paziente selezionato
class HomeActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Applica la lingua scelta
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val sessionManager = SessionManager(this)
        
        // Header con il nome del dottore
        val tvDoctorHeaderName = findViewById<TextView>(R.id.tvDoctorHeaderName)
        val doctorName = sessionManager.getDoctorName()
        if (doctorName != null) {
            tvDoctorHeaderName.text = getString(R.string.bentornato_dott, doctorName)
        }
        
        // Riferimenti alle Card dei calcolatori e storico
        val cardBmi = findViewById<MaterialCardView>(R.id.cardBmi)
        val cardPasi = findViewById<MaterialCardView>(R.id.cardPasi)
        val cardEasi = findViewById<MaterialCardView>(R.id.cardEasi)
        val cardStorico = findViewById<MaterialCardView>(R.id.cardStorico)
        val cardMisurazioniList = findViewById<MaterialCardView>(R.id.cardMisurazioniList)
        
        // Pulsanti di navigazione e info paziente
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnBackToList = findViewById<Button>(R.id.btnBackToList)
        val fabEditPatient = findViewById<FloatingActionButton>(R.id.fabEditPatient)
        val tvSelectedPaziente = findViewById<TextView>(R.id.tvSelectedPaziente)
        val tvPazienteDetails = findViewById<TextView>(R.id.tvPazienteDetails)

        // ID del paziente passato dalla lista
        val pazienteId = intent.getIntExtra("PAZIENTE_ID", -1)
        
        if (pazienteId != -1) {
            val database = AppDatabase.getDatabase(this)
            lifecycleScope.launch {
                val paziente = database.pazienteDao().getById(pazienteId)
                paziente?.let {
                    tvSelectedPaziente.text = "${it.nome} ${it.cognome}"
                    val sessoEsteso = if (it.sesso == "M") getString(R.string.maschio) else getString(R.string.femmina)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
                    tvPazienteDetails.text = "Sesso: $sessoEsteso | Nascita: ${dateFormat.format(it.dataNascita)}"
                }
            }
        }

        // Tasto per modificare l'anagrafica del paziente
        fabEditPatient.setOnClickListener {
            if (pazienteId != -1) {
                val intent = Intent(this, CreatePazienteActivity::class.java)
                intent.putExtra("PAZIENTE_ID", pazienteId)
                startActivity(intent)
            }
        }

        // Torna all'elenco generale
        btnBackToList.setOnClickListener {
            finish()
        }

        // Effettua il logout e torna al login
        btnLogout.setOnClickListener {
            sessionManager.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Navigazione verso i vari calcolatori
        cardBmi.setOnClickListener {
            startActivity(Intent(this, BmiActivity::class.java).apply { putExtra("PAZIENTE_ID", pazienteId) })
        }

        cardPasi.setOnClickListener {
            startActivity(Intent(this, PasiActivity::class.java).apply { putExtra("PAZIENTE_ID", pazienteId) })
        }

        cardEasi.setOnClickListener {
            startActivity(Intent(this, EasiActivity::class.java).apply { putExtra("PAZIENTE_ID", pazienteId) })
        }

        cardStorico.setOnClickListener {
            startActivity(Intent(this, StoricoActivity::class.java).apply { putExtra("PAZIENTE_ID", pazienteId) })
        }

        cardMisurazioniList.setOnClickListener {
            startActivity(Intent(this, MisurazioniListActivity::class.java).apply { putExtra("PAZIENTE_ID", pazienteId) })
        }
    }
}
