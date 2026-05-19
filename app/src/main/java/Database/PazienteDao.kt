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
}
