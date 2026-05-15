package Dermacalc_princ

import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.Spinner
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
        val spinnerBodyPart = findViewById<Spinner>(R.id.spinnerBodyPart)

        btnCalculate.setOnClickListener {
            val eritema = findViewById<SeekBar>(R.id.sbEritema).progress
            val edema = findViewById<SeekBar>(R.id.sbEdema).progress
            val escoriazioni = findViewById<SeekBar>(R.id.sbEscoriazioni).progress
            val lichenificazione = findViewById<SeekBar>(R.id.sbLichenificazione).progress
            val area = findViewById<SeekBar>(R.id.sbArea).progress

            val signsSum = eritema + edema + escoriazioni + lichenificazione
            val bodyPartIndex = spinnerBodyPart.selectedItemPosition

            val risultato = easiCalculator.calculateRegionScore(signsSum, area, bodyPartIndex)
            val severita = easiCalculator.severity(risultato)

            // Il risultato mostrato è il contributo della regione selezionata al totale EASI
            tvResult.text = getString(R.string.risultato_label, "%.1f (%s)".format(risultato, severita))

            salvaEasi(1, risultato, severita) // Placeholder ID paziente
        }
    }

    private fun setupSeekBars() {
        val signsSeekBars = listOf(
            R.id.sbEritema to R.id.tvEritemaValue,
            R.id.sbEdema to R.id.tvEdemaValue,
            R.id.sbEscoriazioni to R.id.tvEscoriazioniValue,
            R.id.sbLichenificazione to R.id.tvLichenificazioneValue
        )

        signsSeekBars.forEach { (sbId, tvId) ->
            val seekBar = findViewById<SeekBar>(sbId)
            val textView = findViewById<TextView>(tvId)
            
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    textView.text = when(progress) {
                        0 -> getString(R.string.desc_zero)
                        1 -> getString(R.string.desc_uno)
                        2 -> getString(R.string.desc_due)
                        3 -> getString(R.string.desc_tre)
                        else -> progress.toString()
                    }
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
            textView.text = getString(R.string.desc_zero)
        }

        val sbArea = findViewById<SeekBar>(R.id.sbArea)
        val tvAreaValue = findViewById<TextView>(R.id.tvAreaValue)
        sbArea.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                tvAreaValue.text = when(progress) {
                    0 -> getString(R.string.area_0)
                    1 -> getString(R.string.area_1)
                    2 -> getString(R.string.area_2)
                    3 -> getString(R.string.area_3)
                    4 -> getString(R.string.area_4)
                    5 -> getString(R.string.area_5)
                    6 -> getString(R.string.area_6)
                    else -> progress.toString()
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        tvAreaValue.text = getString(R.string.area_0)
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
