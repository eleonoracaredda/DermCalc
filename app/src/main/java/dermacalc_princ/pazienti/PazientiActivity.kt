package com.example.dermcalc_princ.pazienti

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
import com.example.dermcalc_princ.R
import com.example.dermcalc_princ.database.AppDatabase
import com.example.dermcalc_princ.auth.RegisterActivity
import com.example.dermcalc_princ.home.HomeActivity
import com.example.dermcalc_princ.utils.SessionManager
import kotlinx.coroutines.launch
import com.example.dermcalc_princ.database.PazienteDao
import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText

import com.example.dermcalc_princ.utils.LocaleHelper
import android.content.Context

// Activity principale per la gestione dell'elenco pazienti associati al medico loggato
class PazientiActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    private lateinit var rvPazienti: RecyclerView
    private lateinit var btnNuovoPaziente: Button
    private lateinit var etSearch: TextInputEditText
    private lateinit var llEmptyState: android.view.View
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
        etSearch = findViewById(R.id.etSearch)
        llEmptyState = findViewById(R.id.llEmptyState)

        // Configurazione del layout manager per la RecyclerView
        rvPazienti.layoutManager = LinearLayoutManager(this)

        // Listener per la ricerca in tempo reale: aggiorna la lista ad ogni carattere digitato
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loadPazienti(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Listener per aggiungere un nuovo paziente: apre la CreatePazienteActivity
        btnNuovoPaziente.setOnClickListener {
            val intent = Intent(this, CreatePazienteActivity::class.java)
            startActivity(intent)
        }

        // Listener per modificare il profilo del medico loggato
        btnEditDoctor.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("EDIT_MODE", true) // Passa un flag per indicare la modalità modifica
            startActivity(intent)
        }

        // Caricamento iniziale dei dati
        loadDoctorProfile()
        loadPazienti()
    }

    override fun onResume() {
        super.onResume()
        // Ricarica i dati quando si torna all'activity (es. dopo aggiunta/modifica)
        loadDoctorProfile()
        loadPazienti(etSearch.text.toString())
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

    // Carica la lista dei pazienti filtrata per il medico corrente e per l'eventuale query di ricerca
    private fun loadPazienti(query: String = "") {
        val doctorId = sessionManager.getDoctorId() ?: return

        lifecycleScope.launch {
            val pazientiList = if (query.isEmpty()) {
                // Recupera tutti i pazienti associati al dottore loggato
                database.pazienteDao().getByDottore(doctorId)
            } else {
                // Esegue la ricerca filtrata
                database.pazienteDao().searchPazienti(doctorId, query)
            }

            // Mostra o nasconde l'empty state
            if (pazientiList.isEmpty()) {
                llEmptyState.visibility = android.view.View.VISIBLE
                rvPazienti.visibility = android.view.View.GONE
            } else {
                llEmptyState.visibility = android.view.View.GONE
                rvPazienti.visibility = android.view.View.VISIBLE
            }

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
