package com.example.dermcalc_princ.calcolatori

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dermcalc_princ.database.AppDatabase
import com.example.dermcalc_princ.dominio.Misurazione
import java.util.Date
import com.example.dermcalc_princ.logic.EasiCalculator
import com.example.dermcalc_princ.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.dermcalc_princ.repository.MisurazioneRepository
import com.example.dermcalc_princ.utils.LocaleHelper
import android.content.Context
import com.google.android.material.chip.ChipGroup

class EasiActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    private lateinit var db: AppDatabase
    private lateinit var repository: MisurazioneRepository
    private val easiCalculator = EasiCalculator()

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
    private lateinit var tvTotalScore: TextView
    private lateinit var tvCurrentRegionScore: TextView
    private lateinit var progressBar: com.google.android.material.progressindicator.LinearProgressIndicator
    private lateinit var tvProgressDetails: TextView
    private lateinit var etNotes: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_easi)

        db = AppDatabase.getDatabase(this)
        repository = MisurazioneRepository(db)

        val pazienteId = intent.getIntExtra("PAZIENTE_ID", -1)
        if (pazienteId == -1) {
            Toast.makeText(this, "Errore: paziente non selezionato", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        initViews()
        setupListeners(pazienteId)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.calcolatore_easi)

        updateTotalUI()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
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
        tvTotalScore = findViewById(R.id.tvTotalScore)
        tvCurrentRegionScore = findViewById(R.id.tvCurrentRegionScore)
        progressBar = findViewById(R.id.progressEvaluation)
        tvProgressDetails = findViewById(R.id.tvProgressDetails)
        etNotes = findViewById(R.id.etNotes)
    }

    private fun setupListeners(pazienteId: Int) {
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupBodyPart)
        
        // Imposta il primo chip come selezionato di default
        chipGroup.check(R.id.chipHead)

        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            currentRegionIndex = when(checkedId) {
                R.id.chipHead -> 0
                R.id.chipTrunk -> 1
                R.id.chipArms -> 2
                R.id.chipLegs -> 3
                else -> 0
            }
            loadRegionData(regions[currentRegionIndex])
            updateTotalUI()
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
            val note = etNotes.text.toString()
            salvaEasi(pazienteId, totalScore, severita, note)
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
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
        val currentRegionData = regions[currentRegionIndex]
        val regionScore = easiCalculator.calculateRegionScore(
            currentRegionData.signsSum(),
            currentRegionData.area,
            currentRegionIndex
        )

        // Aggiorna punteggi
        tvTotalScore.text = "%.1f".format(total)
        tvCurrentRegionScore.text = "Regione: %.1f".format(regionScore)

        // Calcola regioni completate (area > 0)
        val completedRegions = regions.count { it.area > 0 }
        progressBar.progress = completedRegions
        tvProgressDetails.text = "$completedRegions di 4 regioni completate"
    }

    private fun salvaEasi(idPaziente: Int, valore: Double, severita: String, note: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.insertMisurazione(
                Misurazione(
                    pazienteId = idPaziente,
                    tipo = "EASI",
                    valore = valore,
                    severita = severita,
                    data = Date(),
                    note = note
                )
            )
            withContext(Dispatchers.Main) {
                Toast.makeText(this@EasiActivity, "Punteggio Totale salvato!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
