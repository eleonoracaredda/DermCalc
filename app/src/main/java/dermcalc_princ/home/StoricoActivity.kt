package dermcalc_princ.home

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import com.example.dermcalc_princ.R
import database.AppDatabase
import dominio.Misurazione
import repository.MisurazioneRepository
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

            val hasData = misurazioniBmi.isNotEmpty() || misurazioniPasi.isNotEmpty() || misurazioniEasi.isNotEmpty()
            
            findViewById<NestedScrollView>(R.id.nsvContent).visibility = if (hasData) View.VISIBLE else View.GONE
            findViewById<LinearLayout>(R.id.llEmptyState).visibility = if (hasData) View.GONE else View.VISIBLE

            if (hasData) {
                // Configurazione visiva dei tre grafici principali
                setupChart(findViewById(R.id.chartBmi), misurazioniBmi, "BMI", Color.BLUE)
                setupChart(findViewById(R.id.chartPasi), misurazioniPasi, "PASI", Color.RED)
                setupChart(findViewById(R.id.chartEasi), misurazioniEasi, "EASI", Color.GREEN)

                // Popolamento Card di Riepilogo
                updateSummary(misurazioniBmi, R.id.tvLastBmi, R.id.tvDiffBmi)
                updateSummary(misurazioniPasi, R.id.tvLastPasi, R.id.tvDiffPasi)
                updateSummary(misurazioniEasi, R.id.tvLastEasi, R.id.tvDiffEasi)
            }
        }
    }

    private fun updateSummary(data: List<Misurazione>, tvLastId: Int, tvDiffId: Int) {
        val tvLast = findViewById<TextView>(tvLastId)
        val tvDiff = findViewById<TextView>(tvDiffId)

        if (data.isNotEmpty()) {
            val last = data.last()
            tvLast.text = "%.1f".format(last.valore)

            if (data.size > 1) {
                val previous = data[data.size - 2]
                val diff = last.valore - previous.valore
                val diffPercent = (diff / previous.valore) * 100
                
                tvDiff.text = "%s%.1f%%".format(if (diff >= 0) "+" else "", diffPercent)
                tvDiff.setTextColor(if (diff > 0) Color.RED else if (diff < 0) Color.parseColor("#4CAF50") else Color.GRAY)
            }
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
