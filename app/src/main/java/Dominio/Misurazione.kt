package dominio

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

// Entità Room per una singola misurazione clinica
@Entity(tableName = "misurazioni")
data class Misurazione(

    @PrimaryKey(autoGenerate = true) // ID autogenerato
    val id: Int = 0,

    val pazienteId: Int,        // ID del paziente associato

    val tipo: String,           // Tipo di indice (PASI, EASI, BMI, BSA)

    val valore: Double,         // Risultato numerico

    val severita: String,       // Livello di gravità (es. Lieve)

    val data: Date,             // Data della misurazione

    val datiInput: String? = null, // Dati grezzi usati per il calcolo (es. in formato JSON)

    val note: String? = null    // Eventuali annotazioni del medico
)
