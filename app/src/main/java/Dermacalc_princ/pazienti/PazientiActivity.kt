package Dermacalc_princ.pazienti

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Dermcalc_princ.R
import Database.AppDatabase
import Dermacalc_princ.auth.RegisterActivity
import Dermacalc_princ.home.HomeActivity
import Utils.SessionManager
import kotlinx.coroutines.launch
import Database.PazienteDao

// Activity principale per la gestione dell'elenco pazienti associati al medico loggato
class PazientiActivity : AppCompatActivity() {

    private lateinit var rvPazienti: RecyclerView
    private lateinit var btnNuovoPaziente: Button
    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager

    // Elementi del profilo del dottore visualizzati nell'intestazione
    private lateinit var tvDoctorName: TextView
    private lateinit var tvDoctorEmail: TextView
    private lateinit var btnEditDoctor: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pazienti)

        // Inizializzazione database e sessione
        database = AppDatabase.getDatabase(this)
        sessionManager = SessionManager(this)
        
        // Riferimenti alle View
        rvPazienti = findViewById(R.id.rvPazienti)
        btnNuovoPaziente = findViewById(R.id.btnNuovoPaziente)
        tvDoctorName = findViewById(R.id.tvDoctorName)
        tvDoctorEmail = findViewById(R.id.tvDoctorEmail)
        btnEditDoctor = findViewById(R.id.btnEditDoctor)

        rvPazienti.layoutManager = LinearLayoutManager(this)

        // Listener per aggiungere un nuovo paziente
        btnNuovoPaziente.setOnClickListener {
            val intent = Intent(this, CreatePazienteActivity::class.java)
            startActivity(intent)
        }

        // Listener per modificare il profilo del medico
        btnEditDoctor.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("EDIT_MODE", true)
            startActivity(intent)
        }

        loadDoctorProfile()
        loadPazienti()
    }

    override fun onResume() {
        super.onResume()
        // Ricarica i dati quando si torna all'activity (es. dopo aggiunta/modifica)
        loadDoctorProfile()
        loadPazienti()
    }

    // Carica le informazioni del medico dalla sessione e dal database
    private fun loadDoctorProfile() {
        val doctorId = sessionManager.getDoctorId() ?: return
        lifecycleScope.launch {
            val doctor = database.userDao().getUserByTaxCode(doctorId)
            doctor?.let {
                tvDoctorName.text = "Dott. ${it.firstName} ${it.lastName}"
                tvDoctorEmail.text = it.email
            }
        }
    }

    // Carica la lista dei pazienti filtrata per il medico corrente
    private fun loadPazienti() {
        val doctorId = sessionManager.getDoctorId() ?: return

        lifecycleScope.launch {
            // Logica di ricerca/filtro (attualmente con query d'esempio "farmaco")
            val query = "farmaco" 

            val byTerapia = database.pazienteDao().searchByTerapia(query)
            val byNome = database.pazienteDao().searchByNomeCognome(query)

            // Unione dei risultati evitando duplicati
            val pazientiList = (byTerapia + byNome).distinctBy { it.id }

            // Configurazione dell'adapter con le relative callback
            val adapter = PazienteAdapter(
                pazientiList,
                onPazienteClick = { paziente ->
                    // Navigazione alla Home del paziente selezionato
                    val intent = Intent(this@PazientiActivity, HomeActivity::class.java)
                    intent.putExtra("PAZIENTE_ID", paziente.id)
                    startActivity(intent)
                },
                onEditClick = { paziente ->
                    // Navigazione alla modifica dei dati del paziente
                    val intent = Intent(this@PazientiActivity, CreatePazienteActivity::class.java)
                    intent.putExtra("PAZIENTE_ID", paziente.id)
                    startActivity(intent)
                }
            )
            rvPazienti.adapter = adapter
        }
    }
}
