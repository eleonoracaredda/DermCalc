package dermacalc_princ.calcolatori

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
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

class BmiActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    private lateinit var db: AppDatabase
    private lateinit var repository: MisurazioneRepository
    private val bmiCalculator = BmiCalculator()

    private lateinit var tvResultValue: TextView
    private lateinit var tvSeverityLabel: TextView
    private lateinit var ivGaugeIndicator: ImageView
    private lateinit var gaugeContainer: LinearLayout

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
        tvResultValue = findViewById(R.id.tvResultValue)
        tvSeverityLabel = findViewById(R.id.tvSeverityLabel)
        ivGaugeIndicator = findViewById(R.id.ivGaugeIndicator)
        gaugeContainer = findViewById(R.id.gaugeContainer)
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

                // Aggiornamento UI
                tvResultValue.text = String.format("%.1f", bmi)
                tvSeverityLabel.text = severity
                tvSeverityLabel.visibility = View.VISIBLE
                
                updateSeverityUI(bmi, severity)

                salvaBmi(pazienteId, bmi, severity, note, weight, height)
            } else {
                Toast.makeText(this, getString(R.string.compila_tutti_i_campi), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSeverityUI(bmi: Double, severity: String) {
        val color = when (severity) {
            "Sottopeso" -> Color.parseColor("#4FC3F7")
            "Normopeso" -> Color.parseColor("#81C784")
            "Sovrappeso" -> Color.parseColor("#FFF176")
            "Obesità" -> Color.parseColor("#E57373")
            else -> Color.LTGRAY
        }
        
        tvSeverityLabel.backgroundTintList = ColorStateList.valueOf(color)
        if (severity == "Sovrappeso") {
            tvSeverityLabel.setTextColor(Color.BLACK)
        } else {
            tvSeverityLabel.setTextColor(Color.WHITE)
        }

        ivGaugeIndicator.visibility = View.VISIBLE
        ivGaugeIndicator.imageTintList = ColorStateList.valueOf(color)

        gaugeContainer.post {
            val width = gaugeContainer.width.toFloat()
            if (width > 0) {
                val clampedBmi = bmi.coerceIn(0.0, 40.0)
                val ratio = clampedBmi.toFloat() / 40f
                val translationX = (ratio * width) - (ivGaugeIndicator.width / 2f)
                ivGaugeIndicator.animate().translationX(translationX).setDuration(600).start()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun salvaBmi(idPaziente: Int, valore: Double, severita: String, note: String, weight: Double, height: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.insertMisurazione(
                Misurazione(
                    pazienteId = idPaziente,
                    tipo = "BMI",
                    valore = valore,
                    severita = severita,
                    data = Date(),
                    datiInput = "weight:$weight,height:$height",
                    note = note
                )
            )
            withContext(Dispatchers.Main) {
                Toast.makeText(this@BmiActivity, "Punteggio salvato!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
