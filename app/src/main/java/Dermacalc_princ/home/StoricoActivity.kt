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

class StoricoActivity : AppCompatActivity() {

    private lateinit var repository: MisurazioneRepository
    private var pazienteId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storico)

        val db = AppDatabase.getDatabase(this)
        repository = MisurazioneRepository(db)
        pazienteId = intent.getIntExtra("PAZIENTE_ID", -1)

        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        if (pazienteId != -1) {
            loadData()
        }
    }
    private fun loadData() {
        lifecycleScope.launch {
            // Utilizzo del Repository per recuperare i dati filtrati
            val misurazioniBmi = repository.getStoricoPerTipo(pazienteId, "BMI")
            val misurazioniPasi = repository.getStoricoPerTipo(pazienteId, "PASI")
            val misurazioniEasi = repository.getStoricoPerTipo(pazienteId, "EASI")

            setupChart(findViewById(R.id.chartBmi), misurazioniBmi, "BMI", Color.BLUE)
            setupChart(findViewById(R.id.chartPasi), misurazioniPasi, "PASI", Color.RED)
            setupChart(findViewById(R.id.chartEasi), misurazioniEasi, "EASI", Color.GREEN)
        }
    }

    private fun setupChart(chart: LineChart, data: List<Misurazione>, label: String, color: Int) {
        if (data.isEmpty()) {
            chart.setNoDataText(getString(R.string.nessun_dato))
            chart.invalidate()
            return
        }

        val entries = data.mapIndexed { index, misurazione ->
            Entry(index.toFloat(), misurazione.valore.toFloat())
        }

        val dataSet = LineDataSet(entries, label)
        dataSet.color = color
        dataSet.setCircleColor(color)
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.setDrawCircleHole(false)
        dataSet.valueTextSize = 10f
        dataSet.setDrawFilled(true)
        dataSet.fillColor = color
        dataSet.fillAlpha = 50

        val lineData = LineData(dataSet)
        chart.data = lineData

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
        chart.animateX(1000)
        chart.invalidate()
    }
}
