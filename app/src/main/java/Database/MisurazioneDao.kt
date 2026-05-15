package Database

import androidx.room.*
import Dominio.Misurazione

@Dao
interface MisurazioneDao {

    @Insert
    suspend fun insert(m: Misurazione)

    @Query("""
        SELECT * FROM misurazioni
        WHERE pazienteId = :id
        ORDER BY data ASC
    """)
    suspend fun getStorico(id: Int): List<Misurazione>
}