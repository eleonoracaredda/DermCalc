package repository

import Database.AppDatabase
import Dominio.Misurazione
import kotlinx.coroutines.flow.Flow
import java.util.Date

// Gestisce l'accesso ai dati delle misurazioni tramite il DAO
class MisurazioneRepository(private val db: AppDatabase) {

    // Inserisce una nuova misurazione nel DB
    suspend fun insertMisurazione(misurazione: Misurazione) {
        db.misurazioneDao().insert(misurazione)
    }

    // Recupera una misurazione specifica tramite il suo ID
    suspend fun getById(id: Int): Misurazione? {
        return db.misurazioneDao().getById(id)
    }

    // Aggiorna i dati di una misurazione esistente
    suspend fun update(misurazione: Misurazione) {
        db.misurazioneDao().update(misurazione)
    }

    // Elimina una misurazione dal database
    suspend fun delete(misurazione: Misurazione) {
        db.misurazioneDao().delete(misurazione)
    }

    // Storico completo del paziente (Flow per aggiornamenti in tempo reale)
    fun getStoricoPaziente(idPaziente: Int): Flow<List<Misurazione>> {
        return db.misurazioneDao().getStorico(idPaziente)
    }

    // Recupera lo storico filtrato per tipologia (BMI, PASI, ecc.)
    suspend fun getStoricoPerTipo(idPaziente: Int, tipo: String): List<Misurazione> {
        return db.misurazioneDao().getByTipo(idPaziente, tipo)
    }

    // Prende le ultime N misurazioni, utile per i grafici
    suspend fun getUltimeMisurazioni(idPaziente: Int, tipo: String, limit: Int = 10): List<Misurazione> {
        return db.misurazioneDao().getUltime(idPaziente, tipo, limit)
    }

    // Estrae solo i valori numerici per popolare i grafici
    suspend fun getSerieValori(idPaziente: Int, tipo: String): List<Double> {
        return db.misurazioneDao()
            .getByTipo(idPaziente, tipo)
            .map { it.valore }
    }

    // Estrae le date per l'asse X dei grafici
    suspend fun getSerieDate(idPaziente: Int, tipo: String): List<Date> {
        return db.misurazioneDao()
            .getByTipo(idPaziente, tipo)
            .map { it.data }
    }

    // Calcola il minimo e il massimo dei valori registrati
    suspend fun getMinMax(idPaziente: Int, tipo: String): Pair<Double, Double>? {
        val valori = getSerieValori(idPaziente, tipo)
        if (valori.isEmpty()) return null
        return Pair(valori.min(), valori.max())
    }

    // Determina il trend (miglioramento/peggioramento) confrontando prima e ultima misura
    suspend fun getTrend(idPaziente: Int, tipo: String): String {
        val valori = getSerieValori(idPaziente, tipo)
        if (valori.size < 2) return "Nessun trend"

        return when {
            valori.last() > valori.first() -> "In aumento"
            valori.last() < valori.first() -> "In diminuzione"
            else -> "Stabile"
        }
    }
}
