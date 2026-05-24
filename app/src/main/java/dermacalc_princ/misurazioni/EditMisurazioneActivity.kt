package dermacalc_princ.misurazioni

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.dermcalc_princ.R
import Database.AppDatabase
import Dominio.Misurazione
import Repository.MisurazioneRepository
import Utils.LocaleHelper
import Logic.BmiCalculator
import Logic.EasiCalculator
import Logic.PasiCalculator
import Dominio.DatiDistretto
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditMisurazioneActivity : AppCompatActivity() {

    // Mantiene la lingua scelta dall’utente
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    private lateinit var repository: MisurazioneRepository
    private var misurazioneId: Int = -1
    private lateinit var misurazione: Misurazione

    private val bmiCalculator = BmiCalculator()
    private val easiCalculator = EasiCalculator()
    private val pasiCalculator = PasiCalculator()

    private lateinit var containerDatiInput: LinearLayout
    private lateinit var etValore: EditText
    private lateinit var etSeverita: EditText
    private var dynamicInputs = mutableListOf<List<EditText>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_misurazione)

        // Recupero ID della misurazione
        misurazioneId = intent.getIntExtra("MISURAZIONE_ID", -1)

        // Repository
        val db = AppDatabase.getDatabase(this)
        repository = MisurazioneRepository(db)

        // Riferimenti UI
        val tvTipo = findViewById<TextView>(R.id.tvTipo)
        etValore = findViewById(R.id.etValore)
        etSeverita = findViewById(R.id.etSeverita)
        val etNote = findViewById<EditText>(R.id.etNote)
        val tvData = findViewById<TextView>(R.id.tvData)
        containerDatiInput = findViewById(R.id.containerDatiInput)

        val btnSalva = findViewById<Button>(R.id.btnSalva)
        val btnElimina = findViewById<Button>(R.id.btnElimina)
        val btnIndietro = findViewById<Button>(R.id.btnIndietro)

        // Carico la misurazione dal DB
        lifecycleScope.launch {
            misurazione = repository.getById(misurazioneId) ?: return@launch

            // Popolo i campi
            tvTipo.text = misurazione.tipo
            etValore.setText(misurazione.valore.toString())
            etSeverita.setText(misurazione.severita)
            etNote.setText(misurazione.note ?: "")

            val df = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            tvData.text = df.format(misurazione.data)

            updateSeverityStyle(misurazione.severita)
            setupDatiInput()
        }

        // Salvataggio modifiche
        btnSalva.setOnClickListener {
            lifecycleScope.launch {
                val nuovoValore = etValore.text.toString().toDoubleOrNull()
                val nuovaSeverita = etSeverita.text.toString()
                val nuoveNote = etNote.text.toString()

                if (nuovoValore == null) {
                    Toast.makeText(
                        this@EditMisurazioneActivity,
                        "Valore non valido",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val aggiornata = misurazione.copy(
                    valore = nuovoValore,
                    severita = nuovaSeverita,
                    note = nuoveNote,
                    datiInput = recuperaDatiInputCorrenti()
                )

                repository.update(aggiornata)
                Toast.makeText(
                    this@EditMisurazioneActivity,
                    "Modifiche salvate",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }

        // Eliminazione misurazione con conferma
        btnElimina.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Conferma eliminazione")
                .setMessage("Sei sicura di voler eliminare questa misurazione?")
                .setPositiveButton("Elimina") { _, _ ->
                    // Usiamo Dispatchers.IO per l'operazione su DB e non blocchiamo la chiusura dell'activity
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        repository.delete(misurazione)
                    }
                    Toast.makeText(
                        this@EditMisurazioneActivity,
                        "Misurazione eliminata",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .setNegativeButton("Annulla", null)
                .show()
        }

        // Torna indietro
        btnIndietro.setOnClickListener {
            finish()
        }
    }

    private fun setupDatiInput() {
        android.transition.TransitionManager.beginDelayedTransition(containerDatiInput)
        containerDatiInput.removeAllViews()
        dynamicInputs.clear()
        val dati = misurazione.datiInput ?: return
        containerDatiInput.visibility = View.VISIBLE

        when (misurazione.tipo) {
            "BMI" -> setupBmiInputs(dati)
            "EASI" -> setupEasiInputs(dati)
            "PASI" -> setupPasiInputs(dati)
        }
    }

    private fun setupBmiInputs(dati: String) {
        val map = dati.split(",").associate {
            val (k, v) = it.split(":")
            k to v.toDouble()
        }
        val weight = map["weight"] ?: 0.0
        val height = map["height"] ?: 0.0

        val tilWeight = creaTextInputLayout("Peso (kg)", weight.toString())
        val tilHeight = creaTextInputLayout("Altezza (cm)", height.toString())

        containerDatiInput.addView(tilWeight)
        containerDatiInput.addView(tilHeight)

        dynamicInputs.add(listOf(tilWeight.editText as EditText, tilHeight.editText as EditText))

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val w = tilWeight.editText?.text.toString().toDoubleOrNull() ?: 0.0
                val h = tilHeight.editText?.text.toString().toDoubleOrNull() ?: 0.0
                if (h > 0) {
                    val bmi = bmiCalculator.calculate(w, h)
                    val bmiStr = "%.1f".format(bmi).replace(",", ".")
                    etValore.setText(bmiStr)
                    val sev = bmiCalculator.getSeverity(bmi)
                    etSeverita.setText(sev)
                    updateSeverityStyle(sev)
                }
            }
        }

        tilWeight.editText?.addTextChangedListener(watcher)
        tilHeight.editText?.addTextChangedListener(watcher)
    }

    private fun setupEasiInputs(dati: String) {
        val regionsDati = dati.split(";")
        val regionLabels = listOf("Testa/Collo", "Tronco", "Arti Sup", "Arti Inf")
        val fieldLabels = listOf("Er", "Ed", "Es", "Li", "Area")

        regionsDati.forEachIndexed { index, regionDati ->
            if (index >= regionLabels.size) return@forEachIndexed
            val parts = regionDati.split(",")
            val regionInputs = createRegionCard(regionLabels[index], fieldLabels, parts)
            dynamicInputs.add(regionInputs)
        }

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                try {
                    var total = 0.0
                    dynamicInputs.forEachIndexed { index, inputs ->
                        val er = inputs[0].text.toString().toIntOrNull() ?: 0
                        val ed = inputs[1].text.toString().toIntOrNull() ?: 0
                        val es = inputs[2].text.toString().toIntOrNull() ?: 0
                        val li = inputs[3].text.toString().toIntOrNull() ?: 0
                        val area = inputs[4].text.toString().toIntOrNull() ?: 0
                        total += easiCalculator.calculateRegionScore(er + ed + es + li, area, index)
                    }
                    etValore.setText("%.1f".format(total).replace(",", "."))
                    val sev = easiCalculator.severity(total)
                    etSeverita.setText(sev)
                    updateSeverityStyle(sev)
                } catch (_: Exception) {}
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        dynamicInputs.flatten().forEach { it.addTextChangedListener(watcher) }
    }

    private fun setupPasiInputs(dati: String) {
        val regionsDati = dati.split(";")
        val regionLabels = listOf("Testa", "Arti Sup", "Tronco", "Arti Inf")
        val fieldLabels = listOf("Er", "In", "De", "Area")

        regionsDati.forEachIndexed { index, regionDati ->
            if (index >= regionLabels.size) return@forEachIndexed
            val parts = regionDati.split(",")
            val regionInputs = createRegionCard(regionLabels[index], fieldLabels, parts)
            dynamicInputs.add(regionInputs)
        }

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                try {
                    val datiDistretti = dynamicInputs.mapIndexed { index, inputs ->
                        DatiDistretto(
                            eritema = inputs[0].text.toString().toIntOrNull() ?: 0,
                            indurimento = inputs[1].text.toString().toIntOrNull() ?: 0,
                            desquamazione = inputs[2].text.toString().toIntOrNull() ?: 0,
                            area = inputs[3].text.toString().toIntOrNull() ?: 0,
                            peso = when(index) {
                                0 -> 0.1
                                1 -> 0.2
                                2 -> 0.3
                                3 -> 0.4
                                else -> 0.0
                            }
                        )
                    }
                    if (datiDistretti.size >= 4) {
                        val total = pasiCalculator.calculate(datiDistretti[0], datiDistretti[1], datiDistretti[2], datiDistretti[3])
                        etValore.setText("%.1f".format(total).replace(",", "."))
                        val sev = pasiCalculator.severity(total)
                        etSeverita.setText(sev)
                        updateSeverityStyle(sev)
                    }
                } catch (_: Exception) {}
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        dynamicInputs.flatten().forEach { it.addTextChangedListener(watcher) }
    }

    private fun createRegionCard(title: String, fieldLabels: List<String>, values: List<String>): List<EditText> {
        val card = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
            cardElevation = 0f
            radius = 12f
            strokeWidth = 2
            strokeColor = ContextCompat.getColor(this@EditMisurazioneActivity, R.color.backgroundColor)
            setCardBackgroundColor(ContextCompat.getColor(this@EditMisurazioneActivity, R.color.white))
        }

        val innerContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
        }

        val tvLabel = TextView(this).apply {
            text = title
            setTextColor(ContextCompat.getColor(this@EditMisurazioneActivity, R.color.primaryColor))
            setTypeface(null, Typeface.BOLD)
            textSize = 14f
            setPadding(4, 0, 0, 8)
        }
        innerContent.addView(tvLabel)

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val regionInputs = mutableListOf<EditText>()

        fieldLabels.forEachIndexed { i, label ->
            val til = TextInputLayout(this, null, R.style.Widget_DermCalc_EditText).apply {
                hint = label
                val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                if (i < fieldLabels.size - 1) lp.marginEnd = 4
                layoutParams = lp
            }
            val et = TextInputEditText(til.context).apply {
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                setText(values.getOrNull(i) ?: "0")
                textSize = 13f
                setPadding(4, 8, 4, 8)
                gravity = Gravity.CENTER
            }
            til.addView(et)
            row.addView(til)
            regionInputs.add(et)
        }
        innerContent.addView(row)
        card.addView(innerContent)
        containerDatiInput.addView(card)
        return regionInputs
    }

    private fun creaTextInputLayout(label: String, initialValue: String): TextInputLayout {
        val til = TextInputLayout(this, null, R.style.Widget_DermCalc_EditText)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(0, 0, 0, 16)
        til.layoutParams = lp
        til.hint = label

        val et = TextInputEditText(til.context)
        et.setText(initialValue)
        et.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        til.addView(et)

        return til
    }

    private fun recuperaDatiInputCorrenti(): String? {
        return when (misurazione.tipo) {
            "BMI" -> {
                val weight = dynamicInputs.getOrNull(0)?.getOrNull(0)?.text.toString()
                val height = dynamicInputs.getOrNull(0)?.getOrNull(1)?.text.toString()
                "weight:$weight,height:$height"
            }
            "EASI", "PASI" -> {
                dynamicInputs.joinToString(";") { regionInputs ->
                    regionInputs.joinToString(",") { it.text.toString() }
                }
            }
            else -> misurazione.datiInput
        }
    }

    private fun updateSeverityStyle(severity: String) {
        val colorRes = when {
            severity.contains("Assente", ignoreCase = true) ||
                    severity.contains("Chiarito", ignoreCase = true) ||
                    severity.contains("Sottopeso", ignoreCase = true) -> R.color.secondaryColor

            severity.contains("Lieve", ignoreCase = true) ||
                    severity.contains("Normopeso", ignoreCase = true) -> R.color.primaryColor

            severity.contains("Moderata", ignoreCase = true) ||
                    severity.contains("Sovrappeso", ignoreCase = true) -> android.R.color.holo_orange_dark

            severity.contains("Grave", ignoreCase = true) ||
                    severity.contains("Obesità", ignoreCase = true) -> R.color.error

            else -> R.color.textPrimary
        }
        val color = ContextCompat.getColor(this, colorRes)
        etSeverita.setTextColor(color)
        findViewById<TextInputLayout>(R.id.tilSeverita).setBoxStrokeColorStateList(
            android.content.res.ColorStateList.valueOf(color)
        )
    }
}
