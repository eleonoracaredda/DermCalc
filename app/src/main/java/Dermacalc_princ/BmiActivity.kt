package Dermcalc_princ

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import Database.AppDatabase
import Dominio.Misurazione
import Logic.BmiCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BmiActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private val bmiCalculator = BmiCalculator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializza database
        db = AppDatabase.getDatabase(this)

        // ESEMPIO DI VALORI (li sostituirai quando farai il layout)
        val peso = 70.0      // kg
        val altezza = 170.0  // cm

        // Calcolo BMI
        val bmi = bmiCalculator.calculate(peso, altezza)
        val severita = calcolaSeveritaBmi(bmi)

        // ID paziente (placeholder)
        val idPaziente = 1

        salvaBmi(idPaziente, bmi, severita)
    }

    private fun salvaBmi(idPaziente: Int, valore: Double, severita: String) {
        CoroutineScope(Dispatchers.IO).launch {
            db.misurazioneDao().insert(
                Misurazione(
                    pazienteId = idPaziente,
                    tipo = "BMI",
                    valore = valore,
                    severita = severita,
                    data = System.currentTimeMillis()
                )
            )
        }
    }

    private fun calcolaSeveritaBmi(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Sottopeso"
            bmi < 25 -> "Normopeso"
            bmi < 30 -> "Sovrappeso"
            else -> "Obesità"
        }
    }
}
