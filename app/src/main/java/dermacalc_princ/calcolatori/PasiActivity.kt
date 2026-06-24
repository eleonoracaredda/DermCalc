package dermacalc_princ.calcolatori

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import Database.AppDatabase
import Dominio.DatiDistretto
import Dominio.Misurazione
import java.util.Date
import Logic.PasiCalculator
import com.example.dermcalc_princ.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import Repository.MisurazioneRepository
import Utils.LocaleHelper
import android.content.Context
import com.google.android.material.chip.ChipGroup
import com.google.android.material.progressindicator.LinearProgressIndicator

/**
 * Activity per il calcolo dell'indice PASI (Psoriasis Area and Severity Index).
 * Gestisce l'inserimento dei parametri per le quattro regioni corporee e il salvataggio del risultato.
 */
class PasiActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Applica la lingua selezionata dall'utente
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    private lateinit var db: AppDatabase
    private lateinit var repository: MisurazioneRepository
    private val pasiCalculator = PasiCalculator()

    //Struttura dati interna per memorizzare i valori di ogni regione corporea.
    private data class PasiRegionData(
        var eritema: Int = 0,
        var indurimento: Int = 0,
        var desquamazione: Int = 0,
        var area: Int = 0,
        val peso: Double
    )

    // Definisce le quattro regioni PASI con i rispettivi pesi costanti
    private val regions = arrayOf(
        PasiRegionData(peso = 0.1), // Testa
        PasiRegionData(peso = 0.2), // Arti Superiori
        PasiRegionData(peso = 0.3), // Tronco
        PasiRegionData(peso = 0.4)  // Arti Inferiori
    )
    
    private var currentRegionIndex = 0

    // Componenti della UI
    private lateinit var sbEritema: SeekBar
    private lateinit var sbIndurimento: SeekBar
    private lateinit var sbDesquamazione: SeekBar
    private lateinit var sbArea: SeekBar

    private lateinit var tvEritemaVal: TextView
    private lateinit var tvIndurimentoVal: TextView
    private lateinit var tvDesquamazioneVal: TextView
    private lateinit var tvAreaVal: TextView
    private lateinit var tvTotalScore: TextView
    private lateinit var tvCurrentRegionScore: TextView
    private lateinit var tvSeverityLabel: TextView
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var tvProgressDetails: TextView
    private lateinit var etNotes: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pasi)
        
        db = AppDatabase.getDatabase(this)
        repository = MisurazioneRepository(db)
        
        // Recupero l'ID del paziente passato tramite Intent
        val pazienteId = intent.getIntExtra("PAZIENTE_ID", -1)
        if (pazienteId == -1) {
            Toast.makeText(this, "Errore: paziente non selezionato", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupListeners(pazienteId)
        
        // Configurazione Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.calcolatore_pasi)

        // Aggiornamento iniziale dell'interfaccia
        updateTotalUI()
    }

    override fun onSupportNavigateUp(): Boolean {
        // Torna all'activity precedente quando si preme il tasto indietro nella toolbar
        finish()
        return true
    }

    // Inizializza i riferimenti alle view del layout.
    private fun initViews() {
        sbEritema = findViewById(R.id.sbEritema)
        sbIndurimento = findViewById(R.id.sbIndurimento)
        sbDesquamazione = findViewById(R.id.sbDesquamazione)
        sbArea = findViewById(R.id.sbArea)

        tvEritemaVal = findViewById(R.id.tvEritemaValue)
        tvIndurimentoVal = findViewById(R.id.tvIndurimentoValue)
        tvDesquamazioneVal = findViewById(R.id.tvDesquamazioneValue)
        tvAreaVal = findViewById(R.id.tvAreaValue)
        tvTotalScore = findViewById(R.id.tvTotalScore)
        tvSeverityLabel = findViewById(R.id.tvSeverityLabel)
        tvCurrentRegionScore = findViewById(R.id.tvCurrentRegionScore)
        progressBar = findViewById(R.id.progressEvaluation)
        tvProgressDetails = findViewById(R.id.tvProgressDetails)
        etNotes = findViewById<EditText>(R.id.etNotes)
    }

    //Configura i listener per gli eventi della UI.
    private fun setupListeners(pazienteId: Int) {
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupBodyPart)
        
        // Imposta il primo chip (Testa) come selezionato di default
        chipGroup.check(R.id.chipHead)

        // Listener per il cambio della regione corporea tramite Chip
        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            currentRegionIndex = when(checkedId) {
                R.id.chipHead -> 0
                R.id.chipArms -> 1
                R.id.chipTrunk -> 2
                R.id.chipLegs -> 3
                else -> 0
            }
            // Carica i dati salvati per la regione selezionata nelle SeekBar
            loadRegionData(regions[currentRegionIndex])
            updateTotalUI()
        }

        // Listener comune per tutte le SeekBar per aggiornare i calcoli in tempo reale
        val seekBars = listOf(sbEritema, sbIndurimento, sbDesquamazione, sbArea)
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

        // Bottone per salvare la misurazione nel database
        findViewById<Button>(R.id.btnCalculatePasi).setOnClickListener {
            val totalScore = calculateTotalPasi()
            val severita = pasiCalculator.severity(totalScore)
            val note = etNotes.text.toString()

            // Serializza i dati di input per conservarli nella cronologia
            val datiInput = regions.joinToString(";") {
                "${it.eritema},${it.indurimento},${it.desquamazione},${it.area}"
            }

            salvaPasi(pazienteId, totalScore, severita, note, datiInput)
        }

        // Bottone per annullare e tornare indietro
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    // Salva i valori attuali delle SeekBar nell'oggetto regione corrente.
    private fun saveCurrentRegionData() {
        val data = regions[currentRegionIndex]
        data.eritema = sbEritema.progress
        data.indurimento = sbIndurimento.progress
        data.desquamazione = sbDesquamazione.progress
        data.area = sbArea.progress
    }

    // Carica i dati di una regione nelle SeekBar.
    private fun loadRegionData(data: PasiRegionData) {
        sbEritema.progress = data.eritema
        sbIndurimento.progress = data.indurimento
        sbDesquamazione.progress = data.desquamazione
        sbArea.progress = data.area
        updateLabels()
    }

    // Aggiorna le descrizioni testuali sotto le SeekBar in base al progresso.
    private fun updateLabels() {
        tvEritemaVal.text = getSignDesc(sbEritema.progress)
        tvIndurimentoVal.text = getSignDesc(sbIndurimento.progress)
        tvDesquamazioneVal.text = getSignDesc(sbDesquamazione.progress)
        tvAreaVal.text = getAreaDesc(sbArea.progress)
    }

    // Restituisce la descrizione testuale per i parametri di gravità (0-4).
    private fun getSignDesc(progress: Int) = when(progress) {
        0 -> getString(R.string.desc_zero)
        1 -> getString(R.string.desc_uno)
        2 -> getString(R.string.desc_due)
        3 -> getString(R.string.desc_tre)
        4 -> getString(R.string.desc_quattro)
        else -> progress.toString()
    }

    // Restituisce la descrizione testuale per il parametro area (0-6).
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

    // Calcola il punteggio PASI totale sommando i contributi di tutte le regioni.
    private fun calculateTotalPasi(): Double {
        return pasiCalculator.calculate(
            regions[0].toDatiDistretto(),
            regions[1].toDatiDistretto(),
            regions[2].toDatiDistretto(),
            regions[3].toDatiDistretto()
        )
    }


    // Mapper per convertire i dati interni nel formato richiesto dal calcolatore.
    private fun PasiRegionData.toDatiDistretto() = DatiDistretto(
        eritema = eritema,
        indurimento = indurimento,
        desquamazione = desquamazione,
        area = area,
        peso = peso
    )


    // Aggiorna tutti gli elementi dinamici dell'interfaccia (punteggi, badge severità, progresso).
    private fun updateTotalUI() {
        val total = calculateTotalPasi()
        val currentRegionData = regions[currentRegionIndex].toDatiDistretto()
        val currentRegionScore = pasiCalculator.score(currentRegionData)

        // Aggiorna i testi dei punteggi
        tvTotalScore.text = "%.1f".format(total)
        tvCurrentRegionScore.text = getString(R.string.regione_score_default).replace("0.0", "%.1f".format(currentRegionScore))

        // Gestione del badge della severità
        if (total > 0) {
            val severity = pasiCalculator.severity(total)
            tvSeverityLabel.text = severity
            tvSeverityLabel.visibility = android.view.View.VISIBLE
            
            // Imposta il colore in base alla fascia di gravità
            val color = when {
                severity.contains("Lieve", ignoreCase = true) -> android.graphics.Color.parseColor("#81C784")
                severity.contains("Moderata", ignoreCase = true) -> android.graphics.Color.parseColor("#FFB74D")
                severity.contains("Severa", ignoreCase = true) -> android.graphics.Color.parseColor("#E57373")
                else -> android.graphics.Color.LTGRAY
            }
            tvSeverityLabel.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
            if (severity.contains("Lieve", ignoreCase = true)) {
                tvSeverityLabel.setTextColor(android.graphics.Color.BLACK)
            } else {
                tvSeverityLabel.setTextColor(android.graphics.Color.WHITE)
            }
        } else {
            tvSeverityLabel.visibility = android.view.View.GONE
        }

        // Calcola quante regioni hanno un'area superiore a zero per mostrare il progresso
        val completedRegions = regions.count { it.area > 0 }
        progressBar.progress = completedRegions
        tvProgressDetails.text = getString(R.string.regioni_completate_default).replace("0", completedRegions.toString())
    }


    // Inserisce la misurazione nel database in modo asincrono.
    private fun salvaPasi(idPaziente: Int, valore: Double, severita: String, note: String?, datiInput: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.insertMisurazione(
                    Misurazione(
                        pazienteId = idPaziente,
                        tipo = "PASI",
                        valore = valore,
                        severita = severita,
                        data = Date(),
                        datiInput = datiInput,
                        note = note
                    )
                )
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PasiActivity, "Punteggio PASI salvato!", Toast.LENGTH_SHORT).show()
                    finish() // Chiude l'activity dopo il salvataggio
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PasiActivity, "Errore durante il salvataggio", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
