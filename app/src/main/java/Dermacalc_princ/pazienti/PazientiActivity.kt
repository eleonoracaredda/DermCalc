package Dermacalc_princ.pazienti

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Dermcalc_princ.R
import Database.AppDatabase
import Dermacalc_princ.home.HomeActivity
import kotlinx.coroutines.launch

class PazientiActivity : AppCompatActivity() {

    private lateinit var rvPazienti: RecyclerView
    private lateinit var btnNuovoPaziente: Button
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pazienti)

        database = AppDatabase.getDatabase(this)
        rvPazienti = findViewById(R.id.rvPazienti)
        btnNuovoPaziente = findViewById(R.id.btnNuovoPaziente)

        rvPazienti.layoutManager = LinearLayoutManager(this)

        btnNuovoPaziente.setOnClickListener {
            val intent = Intent(this, CreatePazienteActivity::class.java)
            startActivity(intent)
        }

        loadPazienti()
    }

    override fun onResume() {
        super.onResume()
        loadPazienti()
    }

    private fun loadPazienti() {
        lifecycleScope.launch {
            val pazientiList = database.pazienteDao().getAll()
            val adapter = PazienteAdapter(pazientiList) { paziente ->
                // Quando un paziente viene selezionato, apri HomeActivity
                val intent = Intent(this@PazientiActivity, HomeActivity::class.java)
                intent.putExtra("PAZIENTE_ID", paziente.id)
                startActivity(intent)
            }
            rvPazienti.adapter = adapter
        }
    }
}
