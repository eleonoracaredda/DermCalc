package dermacalc_princ.pazienti

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermcalc_princ.R
import Database.AppDatabase
import dermacalc_princ.auth.RegisterActivity
import dermacalc_princ.home.HomeActivity
import Utils.SessionManager
import Utils.LocaleHelper
import kotlinx.coroutines.launch
import com.google.android.material.textfield.TextInputEditText

// Activity principale per la gestione dell'elenco pazienti associati al medico loggato
class PazientiActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Applica la lingua salvata prima di caricare la UI
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    private lateinit var rvPazienti: RecyclerView
    private lateinit var btnNuovoPaziente: com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
    private lateinit var etSearch: TextInputEditText
    private lateinit var llEmptyState: View
    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager

    // Header profilo medico
    private lateinit var tvDoctorName: TextView
    private lateinit var tvDoctorEmail: TextView
    private lateinit var btnEditDoctor: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pazienti)

        // Prepariamo il database e la sessione
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

        rvPazienti.layoutManager = LinearLayoutManager(this)

        // Ricerca istantanea dei pazienti
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loadPazienti(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Tasto per aggiungere un nuovo paziente
        btnNuovoPaziente.setOnClickListener {
            startActivity(Intent(this, CreatePazienteActivity::class.java))
        }

        // Tasto per modificare i dati del medico
        btnEditDoctor.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("EDIT_MODE", true)
            startActivity(intent)
        }

        // Caricamento dati iniziali
        loadDoctorProfile()
        loadPazienti()
    }

    override fun onResume() {
        super.onResume()
        // Aggiorna la lista quando si torna sull'activity
        loadDoctorProfile()
        loadPazienti(etSearch.text.toString())
    }

    // Carica il nome e l'email del dottore nell'header
    private fun loadDoctorProfile() {
        val doctorId = sessionManager.getDoctorId() ?: return
        lifecycleScope.launch {
            val doctor = database.userDao().getUserByTaxCode(doctorId)
            doctor?.let {
                tvDoctorName.text = getString(R.string.dott_format, it.firstName, it.lastName)
                tvDoctorEmail.text = it.email
            }
        }
    }

    // Carica la lista dei pazienti (con eventuale filtro di ricerca)
    private fun loadPazienti(query: String = "") {
        val doctorId = sessionManager.getDoctorId() ?: return

        lifecycleScope.launch {
            val pazientiList = if (query.isEmpty()) {
                database.pazienteDao().getByDottore(doctorId)
            } else {
                database.pazienteDao().searchPazienti(doctorId, query)
            }

            // Gestione del layout se non ci sono pazienti
            if (pazientiList.isEmpty()) {
                llEmptyState.visibility = View.VISIBLE
                rvPazienti.visibility = View.GONE
            } else {
                llEmptyState.visibility = View.GONE
                rvPazienti.visibility = View.VISIBLE
            }

            // Impostiamo l'adapter con le callback per i click
            rvPazienti.adapter = PazienteAdapter(
                pazientiList,
                onPazienteClick = { paziente ->
                    // Naviga alla Home del paziente
                    val intent = Intent(this@PazientiActivity, HomeActivity::class.java)
                    intent.putExtra("PAZIENTE_ID", paziente.id)
                    startActivity(intent)
                },
                onEditClick = { paziente ->
                    // Naviga alla modifica del paziente
                    val intent = Intent(this@PazientiActivity, CreatePazienteActivity::class.java)
                    intent.putExtra("PAZIENTE_ID", paziente.id)
                    startActivity(intent)
                }
            )
        }
    }
}
