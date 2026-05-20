package dermacalc_princ.home

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

    // Riferimenti per la card di riepilogo
    private lateinit var tvLastBmi: TextView
    private lateinit var tvDiffBmi: TextView
    private lateinit var tvLastPasi: TextView
    private lateinit var tvDiffPasi: TextView
    private lateinit var tvLastEasi: TextView
    private lateinit var tvDiffEasi: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storico)

        // Inizializzazione repository
        val db = AppDatabase.getDatabase(this)
        repository = MisurazioneRepository(db)
        
        pazienteId = intent.getIntExtra("PAZIENTE_ID", -1)

        initSummaryViews()

        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        if (pazienteId != -1) {
            loadData()
        }
    }

    private fun initSummaryViews() {
        tvLastBmi = findViewById(R.id.tvLastBmi)
        tvDiffBmi = findViewById(R.id.tvDiffBmi)
        tvLastPasi = findViewById(R.id.tvLastPasi)
        tvDiffPasi = findViewById(R.id.tvDiffPasi)
        tvLastEasi = findViewById(R.id.tvLastEasi)
        tvDiffEasi = findViewById(R.id.tvDiffEasi)
    }
    
    // Carica le misurazioni dal database e popola i grafici
    private fun loadData() {
        lifecycleScope.launch {
            val misurazioniBmi = repository.getStoricoPerTipo(pazienteId, "BMI")
            val misurazioniPasi = repository.getStoricoPerTipo(pazienteId, "PASI")
            val misurazioniEasi = repository.getStoricoPerTipo(pazienteId, "EASI")

            val hasData = misurazioniBmi.isNotEmpty() || misurazioniPasi.isNotEmpty() || misurazioniEasi.isNotEmpty()
            
            val nsvContent = findViewById<View>(R.id.nsvContent)
            val llEmptyState = findViewById<View>(R.id.llEmptyState)
            
            if (hasData) {
                nsvContent.visibility = View.VISIBLE
                llEmptyState.visibility = View.GONE
                
                updateSummary(misurazioniBmi, tvLastBmi, tvDiffBmi)
                updateSummary(misurazioniPasi, tvLastPasi, tvDiffPasi)
                updateSummary(misurazioniEasi, tvLastEasi, tvDiffEasi)

                setupChart(findViewById(R.id.chartBmi), misurazioniBmi, "BMI", ContextCompat.getColor(this@StoricoActivity, R.color.primaryColor))
                setupChart(findViewById(R.id.chartPasi), misurazioniPasi, "PASI", ContextCompat.getColor(this@StoricoActivity, R.color.secondaryColor))
                setupChart(findViewById(R.id.chartEasi), misurazioniEasi, "EASI", ContextCompat.getColor(this@StoricoActivity, R.color.primaryDarkColor))
            } else {
                nsvContent.visibility = View.GONE
                llEmptyState.visibility = View.VISIBLE
            }
        }
    }

    private fun updateSummary(data: List<Misurazione>, tvLast: TextView, tvDiff: TextView) {
        if (data.isEmpty()) {
            tvLast.text = "—"
            tvDiff.text = ""
            return
        }

        val last = data.last()
        tvLast.text = String.format(Locale.getDefault(), "%.1f", last.valore)

        if (data.size >= 2) {
            val prev = data[data.size - 2]
            val diff = last.valore - prev.valore
            val percent = if (prev.valore != 0.0) (diff / prev.valore) * 100 else 0.0
            
            val arrow = if (diff > 0) "▲" else if (diff < 0) "▼" else "—"
            val color = if (diff > 0) Color.RED else if (diff < 0) Color.parseColor("#4CAF50") else Color.GRAY
            
            tvDiff.text = String.format(Locale.getDefault(), "%s %.1f%%", arrow, Math.abs(percent))
            tvDiff.setTextColor(color)
        } else {
            tvDiff.text = "Iniziale"
            tvDiff.setTextColor(Color.GRAY)
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

        val entries = data.mapIndexed { index, misurazione ->
            Entry(index.toFloat(), misurazione.valore.toFloat())
        }

        val dataSet = LineDataSet(entries, label).apply {
            this.color = color
            setCircleColor(color)
            lineWidth = 3f
            circleRadius = 5f
            setDrawCircleHole(true)
            circleHoleColor = Color.WHITE
            circleHoleRadius = 2.5f
            valueTextSize = 10f
            setDrawValues(false)
            
            // Modern gradient fill
            setDrawFilled(true)
            val gradient = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(color, Color.TRANSPARENT)
            )
            fillDrawable = gradient
            fillAlpha = 60
            
            mode = LineDataSet.Mode.CUBIC_BEZIER // Smoother lines
        }

        chart.data = LineData(dataSet)

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
        xAxis.setDrawGridLines(false)

        chart.axisLeft.apply {
            setDrawGridLines(true)
            gridColor = Color.LTGRAY
            gridLineWidth = 0.5f
        }
        chart.axisRight.isEnabled = false

        chart.description.isEnabled = false
        chart.legend.isEnabled = true
        chart.animateX(800)
        chart.invalidate()
    }
}
