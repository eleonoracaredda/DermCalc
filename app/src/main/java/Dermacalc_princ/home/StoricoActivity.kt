package Dermacalc_princ.home

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.Dermcalc_princ.R
import Database.AppDatabase
import Dominio.Misurazione
import Repository.MisurazioneRepository
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Visualizza graficamente l'andamento temporale degli indici clinici (BMI, PASI, EASI) per un paziente specifico
class StoricoActivity : AppCompatActivity() {

    private lateinit var repository: MisurazioneRepository
    private var pazienteId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storico)

        // Inizializzazione repository per l'accesso ai dati delle misurazioni
        val db = AppDatabase.getDatabase(this)
        repository = MisurazioneRepository(db)
        
        // Recupero dell'ID paziente passato tramite Intent
        pazienteId = intent.getIntExtra("PAZIENTE_ID", -1)

        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        if (pazienteId != -1) {
            loadData()
        }
    }
    
    // Carica le misurazioni dal database e popola i grafici
    private fun loadData() {
        lifecycleScope.launch {
            // Recupero dati filtrati per tipologia di calcolatore
            val misurazioniBmi = repository.getStoricoPerTipo(pazienteId, "BMI")
            val misurazioniPasi = repository.getStoricoPerTipo(pazienteId, "PASI")
            val misurazioniEasi = repository.getStoricoPerTipo(pazienteId, "EASI")

            // Configurazione visiva dei tre grafici principali
            setupChart(findViewById(R.id.chartBmi), misurazioniBmi, "BMI", Color.BLUE)
            setupChart(findViewById(R.id.chartPasi), misurazioniPasi, "PASI", Color.RED)
            setupChart(findViewById(R.id.chartEasi), misurazioniEasi, "EASI", Color.GREEN)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // Configura e disegna un grafico a linee (MPAndroidChart) con i dati forniti
    private fun setupChart(chart: LineChart, data: List<Misurazione>, label: String, color: Int) {
        if (data.isEmpty()) {
            chart.setNoDataText(getString(R.string.nessun_dato))
            chart.invalidate()
            return
        }

        // Conversione delle misurazioni in "Entry" del grafico (Index vs Valore)
        val entries = data.mapIndexed { index, misurazione ->
            Entry(index.toFloat(), misurazione.valore.toFloat())
        }

        // Configurazione del set di dati (estetica della linea, punti, colori)
        val dataSet = LineDataSet(entries, label).apply {
            this.color = color
            setCircleColor(color)
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextSize = 10f
            setDrawFilled(true)
            fillColor = color
            fillAlpha = 50
        }

        chart.data = LineData(dataSet)

        // Formattazione asse X per mostrare le date (Giorno/Mese) invece di indici numerici
        val xAxis = chart.xAxis
        xAxis.valueFormatter = object : ValueFormatter() {
            private val mFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index >= 0 && index < data.size) {
                    mFormat.format(data[index].data)
                } else {
                    ""
                }
            }
        }
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f

        chart.description.isEnabled = false
        chart.legend.isEnabled = true
        chart.animateX(1000) // Animazione all'apertura
        chart.invalidate() // Forza il ridisegno
    }
}
