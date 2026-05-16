package Database

import androidx.room.*
import Dominio.Pazienti

// Interfaccia DAO per la gestione dei dati anagrafici dei pazienti
@Dao
interface PazienteDao {

    // Inserisce un nuovo paziente nel database
    @Insert
    suspend fun insert(p: Pazienti)

    // Restituisce la lista completa di tutti i pazienti registrati
    @Query("SELECT * FROM pazienti")
    suspend fun getAll(): List<Pazienti>

    // Cerca e restituisce un paziente specifico tramite il suo ID univoco
    @Query("SELECT * FROM pazienti WHERE id = :id")
    suspend fun getById(id: Int): Pazienti?
}
