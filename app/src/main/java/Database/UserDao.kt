package Database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import Dominio.User

// Interfaccia DAO per la gestione degli utenti (autenticazione e registrazione)
@Dao
interface UserDao {
    
    // Inserisce un utente. Se esiste già un utente con lo stesso taxCode, lo sovrascrive.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    // Verifica le credenziali di accesso cercando un utente con email e password corrispondenti
    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun login(email: String, password: String): User?

    // Cerca un utente specifico tramite il codice fiscale
    @Query("SELECT * FROM users WHERE taxCode = :taxCode")
    suspend fun getUserByTaxCode(taxCode: String): User?

    // controllo email già registrata
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?
}
