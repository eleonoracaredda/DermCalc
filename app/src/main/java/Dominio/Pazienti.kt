package Dominio

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

// Entità Room che rappresenta i dati anagrafici di un paziente
@Entity(tableName = "pazienti")
data class Pazienti(
    @PrimaryKey(autoGenerate = true)     // Identificatore univoco del paziente (generato automaticamente)
    val id: Int = 0,
    val nome: String,           // Nome del paziente
    val cognome: String,        // Cognome del paziente
    val dataNascita: Date,      // Data di nascita del paziente
    val codiceFiscale: String,  // Codice Fiscale del paziente per identificazione univoca
    val dottoreId: String,      // ID (taxCode) del dottore che ha aggiunto il paziente
    val terapia: String? = null, // Terapia
    val dataInizioTerapia: Date? = null, // Data inizio terapia

    )
