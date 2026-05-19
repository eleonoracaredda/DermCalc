package Database

import androidx.room.*
import Dominio.Pazienti

// Interfaccia DAO per la gestione dei dati anagrafici dei pazienti
@Dao
interface PazienteDao {

    // Inserisce un nuovo paziente nel database
    @Insert
    suspend fun insert(p: Pazienti)

    // Aggiorna i dati di un paziente esistente
    @Update
    suspend fun update(p: Pazienti)

    // Restituisce la lista dei pazienti associati a un determinato dottore
    @Query("SELECT * FROM pazienti WHERE dottoreId = :dottoreId")
    suspend fun getByDottore(dottoreId: String): List<Pazienti>

    // Cerca e restituisce un paziente specifico tramite il suo ID univoco
    @Query("SELECT * FROM pazienti WHERE id = :id")
    suspend fun getById(id: Int): Pazienti?

    // Ricerca per terapia (Livello 2 — Punto 7)
    @Query("SELECT * FROM pazienti WHERE terapia LIKE '%' || :farmaco || '%'")
    suspend fun searchByTerapia(farmaco: String): List<Pazienti>

    // Ricerca per nome o cognome (consigliata)
    @Query("SELECT * FROM pazienti WHERE nome LIKE '%' || :query || '%' OR cognome LIKE '%' || :query || '%'")
    suspend fun searchByNomeCognome(query: String): List<Pazienti>
}
