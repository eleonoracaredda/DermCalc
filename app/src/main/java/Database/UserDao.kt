package database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dominio.User

// Interfaccia DAO per la gestione degli utenti (autenticazione e registrazione)
@Dao
interface UserDao {
    
    // Inserisce un utente. Se esiste già un utente con lo stesso taxCode, lo sovrascrive.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    // Cerca un utente specifico tramite il codice fiscale
    @Query("SELECT * FROM users WHERE taxCode = :taxCode")
    suspend fun getUserByTaxCode(taxCode: String): User?

    // controllo / ricerca email già registrata
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    // Aggiorna un utente esistente (per reset password)
    @androidx.room.Update
    suspend fun updateUser(user: User)
}
