package Dermacalc_princ

import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import Database.AppDatabase
import Dominio.Misurazione
import Logic.EasiCalculator
import com.example.Dermcalc_princ.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EasiActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private val easiCalculator = EasiCalculator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_easi)

        db = AppDatabase.getDatabase(this)

        setupSeekBars()

        val btnCalculate = findViewById<Button>(R.id.btnCalculateEasi)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        btnCalculate.setOnClickListener {
            val eritema = findViewById<SeekBar>(R.id.sbEritema).progress
            val edema = findViewById<SeekBar>(R.id.sbEdema).progress
            val escoriazioni = findViewById<SeekBar>(R.id.sbEscoriazioni).progress
            val lichenificazione = findViewById<SeekBar>(R.id.sbLichenificazione).progress

            // Calcolo semplice: somma dei segni per il distretto selezionato
            val score = (eritema + edema + escoriazioni + lichenificazione).toDouble()

            val risultato = easiCalculator.calculate(score)
            val severita = easiCalculator.severity(risultato)

            tvResult.text = getString(R.string.risultato_label, "%.1f (%s)".format(risultato, severita))

            salvaEasi(1, risultato, severita) // Placeholder ID paziente
        }
    }

    private fun setupSeekBars() {
        val seekBars: List<Pair<Int, Int>> = listOf(
            R.id.sbEritema to R.id.tvEritemaValue,
            R.id.sbEdema to R.id.tvEdemaValue,
            R.id.sbEscoriazioni to R.id.tvEscoriazioniValue,
            R.id.sbLichenificazione to R.id.tvLichenificazioneValue,
        )

        seekBars.forEach { (sbId, tvId) ->
            val seekBar = findViewById<SeekBar>(sbId)
            val textView = findViewById<TextView>(tvId)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    textView.text = progress.toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }

    private fun salvaEasi(idPaziente: Int, valore: Double, severita: String) {
        CoroutineScope(Dispatchers.IO).launch {
            db.misurazioneDao().insert(
                Misurazione(
                    pazienteId = idPaziente,
                    tipo = "EASI",
                    valore = valore,
                    severita = severita,
                    data = System.currentTimeMillis()
                )
            )
            withContext(Dispatchers.Main) {
                Toast.makeText(this@EasiActivity, "EASI salvato correttamente", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
