package Dermacalc_princ.calcolatori

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
import Dominio.DatiDistretto
import Dominio.Misurazione
import java.util.Date
import Logic.PasiCalculator
import com.example.Dermcalc_princ.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import Repository.MisurazioneRepository


/**
 * Classe PasiActivity: gestisce l'interfaccia utente per il calcolo del PASI (Psoriasis Area and Severity Index).
 * Permette l'inserimento dei parametri clinici per 4 diverse regioni corporee e il salvataggio del risultato nel database.
 */
class PasiActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var repository: MisurazioneRepository
    private val pasiCalculator = PasiCalculator()

    /**
     * Classe interna per mantenere lo stato dei dati per ciascuna delle 4 regioni corporee del PASI.
     * Ogni regione ha i propri segni clinici (eritema, indurimento, desquamazione), un punteggio d'area e un peso specifico.
     */
    private data class PasiRegionData(
        var eritema: Int = 0,
        var indurimento: Int = 0,
        var desquamazione: Int = 0,
        var area: Int = 0,
        val peso: Double
    )

    /**
     * Array che contiene i dati clinici delle 4 regioni PASI con i relativi pesi standard definiti in letteratura:
     * Testa (10%), Arti Superiori (20%), Tronco (30%), Arti Inferiori (40%).
     */
    private val regions = arrayOf(
        PasiRegionData(peso = 0.1), // Testa
        PasiRegionData(peso = 0.2), // Arti Superiori
        PasiRegionData(peso = 0.3), // Tronco
        PasiRegionData(peso = 0.4)  // Arti Inferiori
    )
    
    // Indice della regione attualmente selezionata nello Spinner
    private var currentRegionIndex = 0

    // Componenti della UI per l'input dei dati
    private lateinit var sbEritema: SeekBar
    private lateinit var sbIndurimento: SeekBar
    private lateinit var sbDesquamazione: SeekBar
    private lateinit var sbArea: SeekBar

    // Componenti della UI per la visualizzazione dei valori e dei risultati
    private lateinit var tvEritemaVal: TextView
    private lateinit var tvIndurimentoVal: TextView
    private lateinit var tvDesquamazioneVal: TextView
    private lateinit var tvAreaVal: TextView
    private lateinit var tvTotalResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pasi)
        
        // Inizializzazione del database Room tramite singleton
        db = AppDatabase.getDatabase(this)
        repository = MisurazioneRepository(db)
        // Recupero ID paziente
        val pazienteId = intent.getIntExtra("PAZIENTE_ID", -1)
        if (pazienteId == -1) {
            Toast.makeText(this, "Errore: paziente non selezionato", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Collegamento delle viste XML al codice e impostazione dei listener
        initViews()
        setupListeners(pazienteId)
        
        // Aggiorna l'interfaccia iniziale per mostrare i valori di default
        updateTotalUI()
    }

    /**
     * Trova e inizializza tutti i riferimenti ai componenti del layout.
     */
    private fun initViews() {
        sbEritema = findViewById(R.id.sbEritema)
        sbIndurimento = findViewById(R.id.sbIndurimento)
        sbDesquamazione = findViewById(R.id.sbDesquamazione)
        sbArea = findViewById(R.id.sbArea)

        tvEritemaVal = findViewById(R.id.tvEritemaValue)
        tvIndurimentoVal = findViewById(R.id.tvIndurimentoValue)
        tvDesquamazioneVal = findViewById(R.id.tvDesquamazioneValue)
        tvAreaVal = findViewById(R.id.tvAreaValue)
        tvTotalResult = findViewById(R.id.tvResult)
    }

    /**
     * Configura i listener per lo Spinner dei distretti e per le SeekBar dei segni clinici.
     */
    private fun setupListeners(pazienteId: Int) {
        val spinnerBodyPart = findViewById<Spinner>(R.id.spinnerBodyPart)

        // Gestisce il cambio di distretto corporeo selezionato
        spinnerBodyPart.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentRegionIndex = position
                // Carica i dati precedentemente salvati per questa regione nelle SeekBar
                loadRegionData(regions[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Lista di tutte le SeekBar per impostare un listener comune
        val seekBars = listOf(sbEritema, sbIndurimento, sbDesquamazione, sbArea)
        seekBars.forEach { sb ->
            sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        // Quando l'utente sposta un cursore, salva i dati, aggiorna le scritte e il punteggio totale
                        saveCurrentRegionData()
                        updateLabels()
                        updateTotalUI()
                    }
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }

        // Pulsante finale per calcolare il punteggio complessivo e salvarlo nel database
        findViewById<Button>(R.id.btnCalculatePasi).setOnClickListener {
            val totalScore = calculateTotalPasi()
            val severita = pasiCalculator.severity(totalScore)
            salvaPasi(pazienteId, totalScore, severita, "") // aggiungere note su XML
        }
    }

    /**
     * Salva i valori attuali delle SeekBar nell'oggetto dati relativo alla regione corrente.
     */
    private fun saveCurrentRegionData() {
        val data = regions[currentRegionIndex]
        data.eritema = sbEritema.progress
        data.indurimento = sbIndurimento.progress
        data.desquamazione = sbDesquamazione.progress
        data.area = sbArea.progress
    }

    /**
     * Ripristina il progresso delle SeekBar in base ai dati salvati per la regione selezionata.
     */
    private fun loadRegionData(data: PasiRegionData) {
        sbEritema.progress = data.eritema
        sbIndurimento.progress = data.indurimento
        sbDesquamazione.progress = data.desquamazione
        sbArea.progress = data.area
        updateLabels()
    }

    /**
     * Aggiorna i testi descrittivi sotto ogni SeekBar in base al valore numerico selezionato.
     */
    private fun updateLabels() {
        tvEritemaVal.text = getSignDesc(sbEritema.progress)
        tvIndurimentoVal.text = getSignDesc(sbIndurimento.progress)
        tvDesquamazioneVal.text = getSignDesc(sbDesquamazione.progress)
        tvAreaVal.text = getAreaDesc(sbArea.progress)
    }

    /**
     * Converte il grado di un segno clinico (0-4) in una descrizione testuale (es. "Lieve").
     */
    private fun getSignDesc(progress: Int) = when(progress) {
        0 -> getString(R.string.desc_zero)
        1 -> getString(R.string.desc_uno)
        2 -> getString(R.string.desc_due)
        3 -> getString(R.string.desc_tre)
        4 -> getString(R.string.desc_quattro)
        else -> progress.toString()
    }

    /**
     * Converte il valore dell'area (0-6) nella relativa fascia percentuale descrittiva.
     */
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

    /**
     * Richiama la logica di calcolo per ottenere il punteggio PASI totale (somma pesata delle 4 regioni).
     */
    private fun calculateTotalPasi(): Double {
        return pasiCalculator.calculate(
            regions[0].toDatiDistretto(),
            regions[1].toDatiDistretto(),
            regions[2].toDatiDistretto(),
            regions[3].toDatiDistretto()
        )
    }

    /**
     * Funzione di estensione per convertire l'oggetto interno PasiRegionData nell'oggetto di dominio DatiDistretto.
     */
    private fun PasiRegionData.toDatiDistretto() = DatiDistretto(
        eritema = eritema,
        indurimento = indurimento,
        desquamazione = desquamazione,
        area = area,
        peso = peso
    )

    /**
     * Aggiorna la visualizzazione del punteggio nella UI (sia parziale che totale).
     */
    private fun updateTotalUI() {
        val total = calculateTotalPasi()
        // Calcolo del punteggio specifico della regione attualmente visibile tramite il calcolatore
        val currentRegionScore = pasiCalculator.score(regions[currentRegionIndex].toDatiDistretto())

        // Imposta il testo formattato usando la risorsa stringa definita in strings.xml
        tvTotalResult.text = getString(R.string.risultato_pasi_label, currentRegionScore, total, pasiCalculator.severity(total))
    }

    /**
     * Effettua il salvataggio asincrono del risultato PASI nel database Room.
     * @param idPaziente Identificativo del paziente.
     * @param valore Il punteggio PASI calcolato.
     * @param severita La categoria di gravità corrispondente al punteggio.
     */
    private fun salvaPasi(idPaziente: Int, valore: Double, severita: String, note: String?) {
        // Avvia una coroutine nel dispatcher IO per le operazioni su DB
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.insertMisurazione(
                    Misurazione(
                        pazienteId = idPaziente,
                        tipo = "PASI",
                        valore = valore,
                        severita = severita,
                        data = Date(),
                        note = note
                    )
                )
                // Ritorna sul thread principale per mostrare il feedback all'utente
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PasiActivity, "Punteggio PASI salvato!", Toast.LENGTH_SHORT).show()
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
