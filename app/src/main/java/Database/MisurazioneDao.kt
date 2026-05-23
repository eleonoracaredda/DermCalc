package Database

import androidx.room.*
import Dominio.Misurazione

// Interfaccia DAO per l'accesso ai dati delle misurazioni nel database
@Dao
interface MisurazioneDao {

    // Inserisce una nuova misurazione nel database
    @Insert
    suspend fun insert(m: Misurazione)

    // Recupera lo storico di tutte le misurazioni di un determinato paziente, ordinate per data crescente
    @Query("""
        SELECT * FROM misurazioni
        WHERE pazienteId = :id
        ORDER BY data ASC
    """)
    suspend fun getStorico(id: Int): List<Misurazione>

    // Storico filtrato per tipo (BMI, PASI, EASI)
    @Query("""
        SELECT * FROM misurazioni
        WHERE pazienteId = :id AND tipo = :tipo
        ORDER BY data ASC
    """)
    suspend fun getByTipo(id: Int, tipo: String): List<Misurazione>

    // Ultime N misurazioni (per grafici)
    @Query("""
        SELECT * FROM misurazioni
        WHERE pazienteId = :id AND tipo = :tipo
        ORDER BY data DESC
        LIMIT :limit
    """)
    suspend fun getUltime(id: Int, tipo: String, limit: Int): List<Misurazione>

    @Query("SELECT * FROM misurazioni WHERE id = :id")
    suspend fun getById(id: Int): Misurazione?

    @Update
    suspend fun update(misurazione: Misurazione)

    @Delete
    suspend fun delete(misurazione: Misurazione)
}
