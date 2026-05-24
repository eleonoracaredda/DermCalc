package Repository

import Database.AppDatabase
import Dominio.Misurazione
import kotlinx.coroutines.flow.Flow
import java.util.Date

class MisurazioneRepository(private val db: AppDatabase) {
    suspend fun insertMisurazione(misurazione: Misurazione) {
        db.misurazioneDao().insert(misurazione)
    }

    suspend fun getById(id: Int): Misurazione? {
        return db.misurazioneDao().getById(id)
    }

    suspend fun update(misurazione: Misurazione) {
        db.misurazioneDao().update(misurazione)
    }

    suspend fun delete(misurazione: Misurazione) {
        db.misurazioneDao().delete(misurazione)
    }

    // Storico completo del paziente (Reattivo tramite Flow)
    fun getStoricoPaziente(idPaziente: Int): Flow<List<Misurazione>> {
        return db.misurazioneDao().getStorico(idPaziente)
    }

    // Storico filtrato per tipo (BMI, PASI, EASI)
    suspend fun getStoricoPerTipo(idPaziente: Int, tipo: String): List<Misurazione> {
        return db.misurazioneDao().getByTipo(idPaziente, tipo)
    }

    // Ultime N misurazioni (per grafici o riepiloghi)
    suspend fun getUltimeMisurazioni(idPaziente: Int, tipo: String, limit: Int = 10): List<Misurazione> {
        return db.misurazioneDao().getUltime(idPaziente, tipo, limit)
    }

    // Serie temporale dei valori (per grafici)
    suspend fun getSerieValori(idPaziente: Int, tipo: String): List<Double> {
        return db.misurazioneDao()
            .getByTipo(idPaziente, tipo)
            .map { it.valore }
    }

    // Serie temporale delle date (per asse X dei grafici)
    suspend fun getSerieDate(idPaziente: Int, tipo: String): List<Date> {
        return db.misurazioneDao()
            .getByTipo(idPaziente, tipo)
            .map { it.data }
    }

    // Min e Max dei valori (per range grafico)
    suspend fun getMinMax(idPaziente: Int, tipo: String): Pair<Double, Double>? {
        val valori = getSerieValori(idPaziente, tipo)
        if (valori.isEmpty()) return null
        return Pair(valori.min(), valori.max())
    }

    // Trend (aumento, diminuzione, stabile)
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
