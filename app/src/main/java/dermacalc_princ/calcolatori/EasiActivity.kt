package dermacalc_princ.calcolatori

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import database.AppDatabase
import dominio.Misurazione
import java.util.Date
import logic.EasiCalculator
import com.example.dermcalc_princ.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import repository.MisurazioneRepository


// Classe EasiActivity: gestisce il calcolo complesso dell'indice EASI per la dermatite atopica
class EasiActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var repository: MisurazioneRepository
    private val easiCalculator = EasiCalculator()

    // Classe interna per mantenere lo stato dei dati per ciascuna delle 4 regioni corporee
    private data class RegionData(
        var eritema: Int = 0,
        var edema: Int = 0,
        var escoriazioni: Int = 0,
        var lichenificazione: Int = 0,
        var area: Int = 0
    ) {
        // Calcola la somma dei quattro segni clinici per la regione
        fun signsSum() = eritema + edema + escoriazioni + lichenificazione
    }

    // Array che contiene i dati delle 4 regioni: Testa, Tronco, Arti Superiori, Arti Inferiori
    private val regions = Array(4) { RegionData() }
    private var currentRegionIndex = 0

    // Componenti della UI (SeekBar per i valori e TextView per le etichette)
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
    private lateinit var etNotes: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_easi)

        // Inizializzazione database
        db = AppDatabase.getDatabase(this)
        repository = MisurazioneRepository(db)

        // Recupero ID paziente
        val pazienteId = intent.getIntExtra("PAZIENTE_ID", -1)
        if (pazienteId == -1) {
            Toast.makeText(this, "Errore: paziente non selezionato", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        // Collegamento viste e setup dei listener
        initViews()
        setupListeners(pazienteId)
        
        // Abilita il pulsante "Indietro" nella barra superiore
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.calcolatore_easi)

        // Aggiorna l'interfaccia con i valori iniziali (tutti a zero)
        updateTotalUI()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // Inizializza i riferimenti agli elementi del layout
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
        etNotes = findViewById<EditText>(R.id.etNotes)
    }

    // Configura i listener per gestire le interazioni dell'utente
    private fun setupListeners(pazienteId: Int) {
        val spinnerBodyPart = findViewById<Spinner>(R.id.spinnerBodyPart)
        
        // Gestisce il cambio della regione corporea tramite lo Spinner
        spinnerBodyPart.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentRegionIndex = position
                // Carica i dati salvati per la nuova regione selezionata nelle SeekBar
                loadRegionData(regions[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Listener per tutte le SeekBar: ogni volta che l'utente sposta un cursore, i dati vengono salvati e il totale aggiornato
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

        // Gestione del pulsante di salvataggio finale
        findViewById<Button>(R.id.btnCalculateEasi).setOnClickListener {
            val totalScore = calculateTotalEasi()
            val severita = easiCalculator.severity(totalScore)
            val note = etNotes.text.toString()
            salvaEasi(pazienteId, totalScore, severita, note)
        }
    }

    // Salva i valori attuali delle SeekBar nell'array delle regioni
    private fun saveCurrentRegionData() {
        val data = regions[currentRegionIndex]
        data.eritema = sbEritema.progress
        data.edema = sbEdema.progress
        data.escoriazioni = sbEscoriazioni.progress
        data.lichenificazione = sbLichenificazione.progress
        data.area = sbArea.progress
    }

    // Imposta il progresso delle SeekBar in base ai dati della regione selezionata
    private fun loadRegionData(data: RegionData) {
        sbEritema.progress = data.eritema
        sbEdema.progress = data.edema
        sbEscoriazioni.progress = data.escoriazioni
        sbLichenificazione.progress = data.lichenificazione
        sbArea.progress = data.area
        updateLabels()
    }

    // Aggiorna le scritte descrittive sotto ogni cursore
    private fun updateLabels() {
        tvEritemaVal.text = getSignDesc(sbEritema.progress)
        tvEdemaVal.text = getSignDesc(sbEdema.progress)
        tvEscoriazioniVal.text = getSignDesc(sbEscoriazioni.progress)
        tvLichenificazioneVal.text = getSignDesc(sbLichenificazione.progress)
        tvAreaVal.text = getAreaDesc(sbArea.progress)
    }

    // Converte il valore numerico del segno in descrizione testuale
    private fun getSignDesc(progress: Int) = when(progress) {
        0 -> getString(R.string.desc_zero)
        1 -> getString(R.string.desc_uno)
        2 -> getString(R.string.desc_due)
        3 -> getString(R.string.desc_tre)
        else -> progress.toString()
    }

    // Converte il valore numerico dell'area in descrizione percentuale
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

    // Esegue la somma dei punteggi ponderati di tutte le 4 regioni
    private fun calculateTotalEasi(): Double {
        var total = 0.0
        regions.forEachIndexed { index, data ->
            total += easiCalculator.calculateRegionScore(data.signsSum(), data.area, index)
        }
        return total
    }

    // Aggiorna la visualizzazione del punteggio parziale e totale nella UI
    private fun updateTotalUI() {
        val total = calculateTotalEasi()
        val regionScore = easiCalculator.calculateRegionScore(
            regions[currentRegionIndex].signsSum(),
            regions[currentRegionIndex].area,
            currentRegionIndex
        )

        tvTotalResult.text = getString(R.string.risultato_label, regionScore, total, easiCalculator.severity(total))
    }

    // Funzione per il salvataggio asincrono nel database locale Room
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
