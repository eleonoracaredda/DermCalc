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
}
