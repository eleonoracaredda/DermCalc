package Dominio

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

// Entità Room che rappresenta una singola misurazione clinica salvata nel database
@Entity(tableName = "misurazioni")
data class Misurazione(

    // Identificatore univoco della misurazione (generato automaticamente)
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // ID del paziente a cui appartiene questa misurazione (chiave esterna logica)
    val pazienteId: Int,

    // Tipologia di indice calcolato: PASI, EASI, BMI, BSA
    val tipo: String,

    // Valore numerico risultante dal calcolo
    val valore: Double,

    // Categoria di gravità associata al valore (es. Lieve, Moderata, Severa)
    val severita: String,

    // Data in cui è stata effettuata la misurazione
    val data: Date
)
