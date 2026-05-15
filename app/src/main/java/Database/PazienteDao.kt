package Database

import androidx.room.*
import Dominio.Pazienti

@Dao
interface PazienteDao {

    @Insert
    suspend fun insert(p: Pazienti)

    @Query("SELECT * FROM pazienti")
    suspend fun getAll(): List<Pazienti>

    @Query("SELECT * FROM pazienti WHERE id = :id")
    suspend fun getById(id: Int): Pazienti?
}