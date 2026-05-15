package Dermacalc_princ

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
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

    // Stato per le 4 regioni (Testa, Tronco, Arti Sup, Arti Inf)
    private data class RegionData(
        var eritema: Int = 0,
        var edema: Int = 0,
        var escoriazioni: Int = 0,
        var lichenificazione: Int = 0,
        var area: Int = 0
    ) {
        fun signsSum() = eritema + edema + escoriazioni + lichenificazione
    }

    private val regions = Array(4) { RegionData() }
    private var currentRegionIndex = 0

    private lateinit var sbEritema: SeekBar
    private lateinit var sbEdema: SeekBar
    private lateinit var sbEscoriazioni: SeekBar
    private lateinit var sbLichenificazione: SeekBar
    private lateinit var sbArea: SeekBar

    private lateinit var tvEritemaVal: TextView
    private lateinit var tvEdemaVal: TextView
    private lateinit var tvEscoriazioniVal: TextView
    private lateinit var tvLichenificazioneVal: TextView
    private lateinit var tvAreaVal: TextView
    private lateinit var tvTotalResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_easi)

        db = AppDatabase.getDatabase(this)
        initViews()
        setupListeners()
        updateTotalUI()
    }

    private fun initViews() {
        sbEritema = findViewById(R.id.sbEritema)
        sbEdema = findViewById(R.id.sbEdema)
        sbEscoriazioni = findViewById(R.id.sbEscoriazioni)
        sbLichenificazione = findViewById(R.id.sbLichenificazione)
        sbArea = findViewById(R.id.sbArea)

        tvEritemaVal = findViewById(R.id.tvEritemaValue)
        tvEdemaVal = findViewById(R.id.tvEdemaValue)
        tvEscoriazioniVal = findViewById(R.id.tvEscoriazioniValue)
        tvLichenificazioneVal = findViewById(R.id.tvLichenificazioneValue)
        tvAreaVal = findViewById(R.id.tvAreaValue)
        tvTotalResult = findViewById(R.id.tvResult)
    }

    private fun setupListeners() {
        val spinnerBodyPart = findViewById<Spinner>(R.id.spinnerBodyPart)
        spinnerBodyPart.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentRegionIndex = position
                loadRegionData(regions[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val seekBars = listOf(sbEritema, sbEdema, sbEscoriazioni, sbLichenificazione, sbArea)
        seekBars.forEach { sb ->
            sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        saveCurrentRegionData()
                        updateLabels()
                        updateTotalUI()
                    }
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }

        findViewById<Button>(R.id.btnCalculateEasi).setOnClickListener {
            val totalScore = calculateTotalEasi()
            val severita = easiCalculator.severity(totalScore)
            salvaEasi(1, totalScore, severita)
        }
    }

    private fun saveCurrentRegionData() {
        val data = regions[currentRegionIndex]
        data.eritema = sbEritema.progress
        data.edema = sbEdema.progress
        data.escoriazioni = sbEscoriazioni.progress
        data.lichenificazione = sbLichenificazione.progress
        data.area = sbArea.progress
    }

    private fun loadRegionData(data: RegionData) {
        sbEritema.progress = data.eritema
        sbEdema.progress = data.edema
        sbEscoriazioni.progress = data.escoriazioni
        sbLichenificazione.progress = data.lichenificazione
        sbArea.progress = data.area
        updateLabels()
    }

    private fun updateLabels() {
        tvEritemaVal.text = getSignDesc(sbEritema.progress)
        tvEdemaVal.text = getSignDesc(sbEdema.progress)
        tvEscoriazioniVal.text = getSignDesc(sbEscoriazioni.progress)
        tvLichenificazioneVal.text = getSignDesc(sbLichenificazione.progress)
        tvAreaVal.text = getAreaDesc(sbArea.progress)
    }

    private fun getSignDesc(progress: Int) = when(progress) {
        0 -> getString(R.string.desc_zero)
        1 -> getString(R.string.desc_uno)
        2 -> getString(R.string.desc_due)
        3 -> getString(R.string.desc_tre)
        else -> progress.toString()
    }

    private fun getAreaDesc(progress: Int) = when(progress) {
        0 -> getString(R.string.area_0)
        1 -> getString(R.string.area_1)
        2 -> getString(R.string.area_2)
        3 -> getString(R.string.area_3)
        4 -> getString(R.string.area_4)
        5 -> getString(R.string.area_5)
        6 -> getString(R.string.area_6)
        else -> progress.toString()
    }

    private fun calculateTotalEasi(): Double {
        var total = 0.0
        regions.forEachIndexed { index, data ->
            total += easiCalculator.calculateRegionScore(data.signsSum(), data.area, index)
        }
        return total
    }

    private fun updateTotalUI() {
        val total = calculateTotalEasi()
        val regionScore = easiCalculator.calculateRegionScore(
            regions[currentRegionIndex].signsSum(),
            regions[currentRegionIndex].area,
            currentRegionIndex
        )

        tvTotalResult.text = getString(R.string.risultato_label, regionScore, total, easiCalculator.severity(total))
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
                Toast.makeText(this@EasiActivity, "Punteggio Totale salvato!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
