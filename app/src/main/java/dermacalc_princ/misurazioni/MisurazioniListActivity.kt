package dermacalc_princ.misurazioni

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermcalc_princ.R
import database.AppDatabase
import repository.MisurazioneRepository
import Utils.LocaleHelper
import kotlinx.coroutines.launch

class MisurazioniListActivity : AppCompatActivity() {

    // Mantiene la lingua scelta dall’utente
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    private lateinit var repository: MisurazioneRepository
    private var pazienteId: Int = -1   // ID del paziente di cui mostrare lo storico

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_misurazioni_list)

        // Recupero l’ID del paziente passato dalla HomeActivity
        pazienteId = intent.getIntExtra("PAZIENTE_ID", -1)

        // Repository per leggere le misurazioni dal DB
        val db = AppDatabase.getDatabase(this)
        repository = MisurazioneRepository(db)

        val rv = findViewById<RecyclerView>(R.id.rvMisurazioni)
        val empty = findViewById<View>(R.id.llEmptyState)
        val btnBack = findViewById<Button>(R.id.btnBack)

        rv.layoutManager = LinearLayoutManager(this)

        btnBack.setOnClickListener { finish() }

        // Carico le misurazioni del paziente in modo reattivo
        lifecycleScope.launch {
            repository.getStoricoPaziente(pazienteId).collect { lista ->
                // Se non ci sono misurazioni → mostro empty state
                if (lista.isEmpty()) {
                    empty.visibility = View.VISIBLE
                    rv.visibility = View.GONE
                } else {
                    empty.visibility = View.GONE
                    rv.visibility = View.VISIBLE

                    // Adapter con click per aprire la modifica
                    rv.adapter = MisurazioniAdapter(lista) { mis ->
                        val intent =
                            Intent(this@MisurazioniListActivity, EditMisurazioneActivity::class.java)
                        intent.putExtra("MISURAZIONE_ID", mis.id)
                        startActivity(intent)
                    }
                }
            }
        }
    }
}

