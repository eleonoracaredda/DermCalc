package Dominio

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entità Room che rappresenta un utente registrato nell'applicazione (medico o operatore)
@Entity(tableName = "users")
data class User(
    // Il codice fiscale viene usato come chiave primaria univoca
    @PrimaryKey val taxCode: String,
    
    // Nome dell'utente
    val firstName: String,
    
    // Cognome dell'utente
    val lastName: String,
    
    // Indirizzo email per il login
    val email: String,
    
    // Password (idealmente dovrebbe essere salvata come hash)
    val password: String
)
