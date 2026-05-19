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

class PazientiActivity : AppCompatActivity() {

    private lateinit var rvPazienti: RecyclerView
    private lateinit var btnNuovoPaziente: Button
    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager

    // Elementi profilo dottore
    private lateinit var tvDoctorName: TextView
    private lateinit var tvDoctorEmail: TextView
    private lateinit var btnEditDoctor: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pazienti)

        database = AppDatabase.getDatabase(this)
        sessionManager = SessionManager(this)
        rvPazienti = findViewById(R.id.rvPazienti)
        btnNuovoPaziente = findViewById(R.id.btnNuovoPaziente)

        tvDoctorName = findViewById(R.id.tvDoctorName)
        tvDoctorEmail = findViewById(R.id.tvDoctorEmail)
        btnEditDoctor = findViewById(R.id.btnEditDoctor)

        rvPazienti.layoutManager = LinearLayoutManager(this)

        btnNuovoPaziente.setOnClickListener {
            val intent = Intent(this, CreatePazienteActivity::class.java)
            startActivity(intent)
        }

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
        loadDoctorProfile()
        loadPazienti()
    }

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

    private fun loadPazienti() {
        val doctorId = sessionManager.getDoctorId() ?: return
        
        lifecycleScope.launch {
            val pazientiList = database.pazienteDao().getByDottore(doctorId)
            val adapter = PazienteAdapter(
                pazientiList,
                onPazienteClick = { paziente ->
                    // Quando un paziente viene selezionato, apri HomeActivity
                    val intent = Intent(this@PazientiActivity, HomeActivity::class.java)
                    intent.putExtra("PAZIENTE_ID", paziente.id)
                    startActivity(intent)
                },
                onEditClick = { paziente ->
                    // Quando si clicca su modifica, apri CreatePazienteActivity in modalità edit
                    val intent = Intent(this@PazientiActivity, CreatePazienteActivity::class.java)
                    intent.putExtra("PAZIENTE_ID", paziente.id)
                    startActivity(intent)
                }
            )
            rvPazienti.adapter = adapter
        }
    }
}
