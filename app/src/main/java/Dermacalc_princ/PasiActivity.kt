package Dermacalc_princ

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import Database.AppDatabase
import Dominio.DatiDistretto
import Dominio.Misurazione
import Logic.PasiCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Classe PasiActivity: gestisce l'integrazione del calcolo PASI con il database
class PasiActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private val pasiCalculator = PasiCalculator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializzazione del database Room
        db = AppDatabase.getDatabase(this)

        // ESEMPIO DI DATI CLINICI (Valori da 0 a 4 per i segni clinici)
        // In una versione finale, questi dati verrebbero raccolti da SeekBar o input dell'utente
        val testa = DatiDistretto(
            eritema = 2,
            indurimento = 1,
            desquamazione = 3,
            area = 2,
            peso = 0.1 // Peso specifico del distretto "testa"
        )

        val artiSup = DatiDistretto(1, 1, 2, 2, 0.2) // Arti superiori: peso 20%
        val tronco = DatiDistretto(2, 2, 2, 3, 0.3)  // Tronco: peso 30%
        val artiInf = DatiDistretto(3, 2, 1, 3, 0.4)  // Arti inferiori: peso 40%

        // Esegue il calcolo del punteggio PASI totale
        val risultato = pasiCalculator.calculate(testa, artiSup, tronco, artiInf)
        
        // Determina la categoria di severità basata sul punteggio (Lieve, Moderata, Severa)
        val severita = pasiCalculator.severity(risultato)

        // Identificativo del paziente (attualmente impostato a 1 per scopi dimostrativi)
        val idPaziente = 1

        // Avvia il processo di salvataggio nel database
        salvaPasi(idPaziente, risultato, severita)
    }

    // Metodo privato per il salvataggio asincrono dei risultati nel database locale
    private fun salvaPasi(idPaziente: Int, valore: Double, severita: String) {
        // Utilizza una coroutine per non bloccare il thread dell'interfaccia utente (UI Thread)
        CoroutineScope(Dispatchers.IO).launch {
            db.misurazioneDao().insert(
                Misurazione(
                    pazienteId = idPaziente,
                    tipo = "PASI",
                    valore = valore,
                    severita = severita,
                    data = System.currentTimeMillis() // Salva l'orario attuale in millisecondi
                )
            )
        }
    }
}
