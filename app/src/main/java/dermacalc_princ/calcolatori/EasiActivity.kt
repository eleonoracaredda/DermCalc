package dermacalc_princ.calcolatori

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import Database.AppDatabase
import Dominio.Misurazione
import java.util.Date
import Logic.EasiCalculator
import com.example.dermcalc_princ.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import repository.MisurazioneRepository
import Utils.LocaleHelper
import com.google.android.material.chip.ChipGroup
import com.google.android.material.progressindicator.LinearProgressIndicator

/**
 * Activity per il calcolo dell'indice EASI (Eczema Area and Severity Index)
 * Permette la valutazione di 4 regioni corporee e calcola un punteggio totale
 */
class EasiActivity : AppCompatActivity() {

    //Applicazione della lingua selezionata all'Activity
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    private lateinit var db: AppDatabase
    private lateinit var repository: MisurazioneRepository
    private val easiCalculator = EasiCalculator()

    /**
     * Struttura dati che rappresenta una regione corporea
     * con i parametri clinici valutati
     */
    private data class RegionData(
        var eritema: Int = 0,
        var edema: Int = 0,
        var escoriazioni: Int = 0,
        var lichenificazione: Int = 0,
        var area: Int = 0
    ) {
        fun signsSum() = eritema + edema + escoriazioni + lichenificazione
    }

    // Array delle 4 regioni corporee (testa, tronco, braccia, gambe)
    private val regions = Array(4) { RegionData() }
    private var currentRegionIndex = 0

    //Elementi UI
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
    private lateinit var tvSeverityLabel: TextView
    private lateinit var tvCurrentRegionScore: TextView
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var tvProgressDetails: TextView
    private lateinit var etNotes: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_easi)

        db = AppDatabase.getDatabase(this)
        repository = MisurazioneRepository(db)

        //Controllo validità paziente
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

    //Gestione pulsante back della toolbar
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    //Inizializza tutte le view della UI
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
        tvSeverityLabel = findViewById(R.id.tvSeverityLabel)
        tvCurrentRegionScore = findViewById(R.id.tvCurrentRegionScore)
        progressBar = findViewById(R.id.progressEvaluation)
        tvProgressDetails = findViewById(R.id.tvProgressDetails)
        etNotes = findViewById(R.id.etNotes)
    }

    // Configura listener per ChipGroup, SeekBar e pulsanti
    private fun setupListeners(pazienteId: Int) {
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupBodyPart)
        chipGroup.check(R.id.chipHead)

        // Cambio regione corporea selezionata
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

        // Listener per aggiornamento automatico valori SeekBar
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

        // Calcolo finale EASI
        findViewById<Button>(R.id.btnCalculateEasi).setOnClickListener {
            val totalScore = calculateTotalEasi()
            val severita = easiCalculator.severity(totalScore)
            val note = etNotes.text.toString()

            val datiInput = regions.joinToString(";") {
                "${it.eritema},${it.edema},${it.escoriazioni},${it.lichenificazione},${it.area}"
            }

            salvaEasi(pazienteId, totalScore, severita, note, datiInput)
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    //Salva i dati della regione corrente
    private fun saveCurrentRegionData() {
        val data = regions[currentRegionIndex]
        data.eritema = sbEritema.progress
        data.edema = sbEdema.progress
        data.escoriazioni = sbEscoriazioni.progress
        data.lichenificazione = sbLichenificazione.progress
        data.area = sbArea.progress
    }

    //Carica i dati della regione selezionata nella UI
    private fun loadRegionData(data: RegionData) {
        sbEritema.progress = data.eritema
        sbEdema.progress = data.edema
        sbEscoriazioni.progress = data.escoriazioni
        sbLichenificazione.progress = data.lichenificazione
        sbArea.progress = data.area
        updateLabels()
    }

    //Aggiorna le etichette descrittive dei valori
    private fun updateLabels() {
        tvEritemaVal.text = getSignDesc(sbEritema.progress)
        tvEdemaVal.text = getSignDesc(sbEdema.progress)
        tvEscoriazioniVal.text = getSignDesc(sbEscoriazioni.progress)
        tvLichenificazioneVal.text = getSignDesc(sbLichenificazione.progress)
        tvAreaVal.text = getAreaDesc(sbArea.progress)
    }

    //Conversione valori segni clinici in descrizione testuale
    private fun getSignDesc(progress: Int) = when(progress) {
        0 -> getString(R.string.desc_zero)
        1 -> getString(R.string.desc_uno)
        2 -> getString(R.string.desc_due)
        3 -> getString(R.string.desc_tre)
        else -> progress.toString()
    }

    //Conversione area in descrizione testuale
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

    //Calcolo punteggio totale EASI su tutte le regioni
    private fun calculateTotalEasi(): Double {
        var total = 0.0
        regions.forEachIndexed { index, data ->
            total += easiCalculator.calculateRegionScore(data.signsSum(), data.area, index)
        }
        return total
    }

    //Aggiorna tutta la UI (punteggio, severità, progressi)
    private fun updateTotalUI() {
        val total = calculateTotalEasi()
        val currentRegionData = regions[currentRegionIndex]
        val regionScore = easiCalculator.calculateRegionScore(
            currentRegionData.signsSum(),
            currentRegionData.area,
            currentRegionIndex
        )

        // Aggiorna punteggi
        tvTotalScore.text = String.format("%.1f", total)
        tvCurrentRegionScore.text = getString(R.string.regione_score_default).replace("0.0", String.format("%.1f", regionScore))

        // Aggiorna Severità visiva
        if (total > 0) {
            val severity = easiCalculator.severity(total)
            tvSeverityLabel.text = severity
            tvSeverityLabel.visibility = View.VISIBLE
            
            val color = when (severity) {
                "Assente", "Eczema chiarito" -> Color.parseColor("#81C784")
                "Dermatite Lieve" -> Color.parseColor("#FFF176")
                "Dermatite Moderata" -> Color.parseColor("#FFB74D")
                "Dermatite Severa" -> Color.parseColor("#E57373")
                else -> Color.LTGRAY
            }
            tvSeverityLabel.backgroundTintList = ColorStateList.valueOf(color)
            if (severity == "Dermatite Lieve") {
                tvSeverityLabel.setTextColor(Color.BLACK)
            } else {
                tvSeverityLabel.setTextColor(Color.WHITE)
            }
        } else {
            tvSeverityLabel.visibility = View.GONE
        }

        // Calcola regioni completate (area > 0)
        val completedRegions = regions.count { it.area > 0 }
        progressBar.progress = completedRegions
        tvProgressDetails.text = getString(R.string.regioni_completate_default).replace("0", completedRegions.toString())
    }

    //Salvataggio misurazione EASI su database (thread IO)
    private fun salvaEasi(idPaziente: Int, valore: Double, severita: String, note: String?, datiInput: String) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.insertMisurazione(
                Misurazione(
                    pazienteId = idPaziente,
                    tipo = "EASI",
                    valore = valore,
                    severita = severita,
                    data = Date(),
                    datiInput = datiInput,
                    note = note
                )
            )
            withContext(Dispatchers.Main) {
                Toast.makeText(this@EasiActivity, "Punteggio salvato!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
