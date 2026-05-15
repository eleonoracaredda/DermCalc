package Dominio

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pazienti")
data class Pazienti(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nome: String,
    val cognome: String,
    val dataNascita: String,
    val codiceFiscale: String
)