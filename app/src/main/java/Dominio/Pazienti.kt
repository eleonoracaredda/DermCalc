package Dominio

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

// Entità Room che rappresenta i dati anagrafici di un paziente
@Entity(tableName = "pazienti")
data class Pazienti(

    // Identificatore univoco del paziente (generato automaticamente)
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Nome del paziente
    val nome: String,

    //Codice fiscale
    val codFiscale: String,
    
    // Cognome del paziente
    val cognome: String,
    
    // Data di nascita del paziente
    val dataNascita: Date,
    
    // Codice Fiscale del paziente per identificazione univoca
    val codiceFiscale: String
)
