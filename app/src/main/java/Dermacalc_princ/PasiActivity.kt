package Dermcalc_princ

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import Database.AppDatabase
import Dominio.DatiDistretto
import Dominio.Misurazione
import Logic.PasiCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PasiActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private val pasiCalculator = PasiCalculator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializza database
        db = AppDatabase.getDatabase(this)

        // ESEMPIO DI VALORI (0–4)
        val testa = DatiDistretto(
            eritema = 2,
            indurimento = 1,
            desquamazione = 3,
            area = 2,
            peso = 0.1
        )

        val artiSup = DatiDistretto(1, 1, 2, 2, 0.2)
        val tronco = DatiDistretto(2, 2, 2, 3, 0.3)
        val artiInf = DatiDistretto(3, 2, 1, 3, 0.4)

        // Calcolo PASI
        val risultato = pasiCalculator.calculate(testa, artiSup, tronco, artiInf)
        val severita = pasiCalculator.severity(risultato)

        // ID paziente (per ora fittizio)
        val idPaziente = 1

        salvaPasi(idPaziente, risultato, severita)
    }

    private fun salvaPasi(idPaziente: Int, valore: Double, severita: String) {
        CoroutineScope(Dispatchers.IO).launch {
            db.misurazioneDao().insert(
                Misurazione(
                    pazienteId = idPaziente,
                    tipo = "PASI",
                    valore = valore,
                    severita = severita,
                    data = System.currentTimeMillis()
                )
            )
        }
    }
}

