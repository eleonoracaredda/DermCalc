package dermacalc_princ.calcolatori

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import Database.AppDatabase
import Dominio.Misurazione
import java.util.Date
import Logic.BmiCalculator
import com.example.dermcalc_princ.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import Repository.MisurazioneRepository
import Utils.LocaleHelper
import android.content.Context

class BmiActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    private lateinit var db: AppDatabase
    private lateinit var repository: MisurazioneRepository
    private val bmiCalculator = BmiCalculator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bmi)

        db = AppDatabase.getDatabase(this)
        repository = MisurazioneRepository(db)

        val pazienteId = intent.getIntExtra("PAZIENTE_ID", -1)
        if (pazienteId == -1) {
            Toast.makeText(this, "Errore: paziente non selezionato", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val etWeight = findViewById<EditText>(R.id.etWeight)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val etNotes = findViewById<EditText>(R.id.etNotes)
        val btnCalculate = findViewById<Button>(R.id.btnCalculateBmi)
        val tvResultValue = findViewById<TextView>(R.id.tvResultValue)
        val tvSeverityLabel = findViewById<TextView>(R.id.tvSeverityLabel)
        val btnBack = findViewById<Button>(R.id.btnBack)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.calcolatore_bmi)

        btnBack.setOnClickListener {
            finish()
        }

        btnCalculate.setOnClickListener {
            val weightStr = etWeight.text.toString()
            val heightStr = etHeight.text.toString()
            val note = etNotes.text.toString()

            if (weightStr.isNotEmpty() && heightStr.isNotEmpty()) {
                val weight = weightStr.toDouble()
                val height = heightStr.toDouble()

                val bmi = bmiCalculator.calculate(weight, height)
                val severity = bmiCalculator.getSeverity(bmi)

                // Aggiornamento UI con i nuovi componenti
                tvResultValue.text = "%.1f".format(bmi)
                tvSeverityLabel.text = severity

                salvaBmi(pazienteId, bmi, severity, note)
            } else {
                Toast.makeText(this, "Inserisci tutti i valori", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun salvaBmi(idPaziente: Int, valore: Double, severita: String, note: String) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.insertMisurazione(
                Misurazione(
                    pazienteId = idPaziente,
                    tipo = "BMI",
                    valore = valore,
                    severita = severita,
                    data = Date(),
                    note = note
                )
            )
            withContext(Dispatchers.Main) {
                Toast.makeText(this@BmiActivity, "Salvato nel database", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
