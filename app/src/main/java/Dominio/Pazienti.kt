package dominio

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

// Entità Room per i dati anagrafici dei pazienti
@Entity(tableName = "pazienti")
data class Pazienti(
    @PrimaryKey(autoGenerate = true) // ID autogenerato
    val id: Int = 0,
    val nome: String,                // Nome
    val cognome: String,             // Cognome
    val dataNascita: Date,           // Data di nascita
    val codiceFiscale: String,       // Codice Fiscale (univoco per legge)
    val dottoreId: String,           // ID del dottore proprietario del record
    val terapia: String? = null,     // Terapia attuale (opzionale)
    val dataInizioTerapia: Date? = null, // Inizio del trattamento
    val sesso: String = "M",         // Sesso (M/F)
    val consenso: Boolean = false,   // Stato del consenso al trattamento dati
    val comorbilita : String? = null, // Eventuali altre patologie
    val caregiverNome: String? = null, // Contatto di emergenza: Nome
    val caregiverTelefono: String? = null // Contatto di emergenza: Telefono
)
