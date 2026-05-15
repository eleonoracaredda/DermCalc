package Dermcalc_princ

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import Database.AppDatabase
import Dominio.Misurazione
import Logic.EasiCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EasiActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private val easiCalculator = EasiCalculator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializza database
        db = AppDatabase.getDatabase(this)

        // ESEMPIO DI VALORE EASI (lo sostituirai con input reali quando farai il layout)
        val score = 12.5

        // Calcolo EASI
        val risultato = easiCalculator.calculate(score)
        val severita = easiCalculator.severity(risultato)

        // ID paziente (placeholder)
        val idPaziente = 1

        salvaEasi(idPaziente, risultato, severita)
    }

    private fun salvaEasi(idPaziente: Int, valore: Double, severita: String) {
        CoroutineScope(Dispatchers.IO).launch {
            db.misurazioneDao().insert(
                Misurazione(
                    pazienteId = idPaziente,
                    tipo = "EASI",
                    valore = valore,
                    severita = severita,
                    data = System.currentTimeMillis()
                )
            )
        }
    }
}
