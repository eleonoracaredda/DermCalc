package dermacalc_princ.misurazioni

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dermcalc_princ.R
import Database.AppDatabase
import Dominio.Misurazione
import Repository.MisurazioneRepository
import Utils.LocaleHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditMisurazioneActivity : AppCompatActivity() {

    // Mantiene la lingua scelta dall’utente
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    private lateinit var repository: MisurazioneRepository
    private var misurazioneId: Int = -1
    private lateinit var misurazione: Misurazione

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_misurazione)

        // Recupero ID della misurazione
        misurazioneId = intent.getIntExtra("MISURAZIONE_ID", -1)

        // Repository
        val db = AppDatabase.getDatabase(this)
        repository = MisurazioneRepository(db)

        // Riferimenti UI
        val tvTipo = findViewById<TextView>(R.id.tvTipo)
        val etValore = findViewById<EditText>(R.id.etValore)
        val etSeverita = findViewById<EditText>(R.id.etSeverita)
        val etNote = findViewById<EditText>(R.id.etNote)
        val tvData = findViewById<TextView>(R.id.tvData)

        val btnSalva = findViewById<Button>(R.id.btnSalva)
        val btnElimina = findViewById<Button>(R.id.btnElimina)
        val btnIndietro = findViewById<Button>(R.id.btnIndietro)

        // Carico la misurazione dal DB
        lifecycleScope.launch {
            misurazione = repository.getById(misurazioneId) ?: return@launch

            // Popolo i campi
            tvTipo.text = misurazione.tipo
            etValore.setText(misurazione.valore.toString())
            etSeverita.setText(misurazione.severita)
            etNote.setText(misurazione.note ?: "")

            val df = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            tvData.text = df.format(misurazione.data)
        }

        // Salvataggio modifiche
        btnSalva.setOnClickListener {
            lifecycleScope.launch {
                val nuovoValore = etValore.text.toString().toDoubleOrNull()
                val nuovaSeverita = etSeverita.text.toString()
                val nuoveNote = etNote.text.toString()

                if (nuovoValore == null) {
                    Toast.makeText(
                        this@EditMisurazioneActivity,
                        "Valore non valido",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val aggiornata = misurazione.copy(
                    valore = nuovoValore,
                    severita = nuovaSeverita,
                    note = nuoveNote
                )

                repository.update(aggiornata)
                Toast.makeText(
                    this@EditMisurazioneActivity,
                    "Modifiche salvate",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }

        // Eliminazione misurazione con conferma
        btnElimina.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Conferma eliminazione")
                .setMessage("Sei sicura di voler eliminare questa misurazione?")
                .setPositiveButton("Elimina") { _, _ ->
                    lifecycleScope.launch {
                        repository.delete(misurazione)
                        Toast.makeText(
                            this@EditMisurazioneActivity,
                            "Misurazione eliminata",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
                .setNegativeButton("Annulla", null)
                .show()
        }

        // Torna indietro
        btnIndietro.setOnClickListener {
            finish()
        }
    }
}
