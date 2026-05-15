package Dominio

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "misurazioni")
data class Misurazione(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val pazienteId: Int,

    val tipo: String,   // PASI, EASI, BMI, BSA

    val valore: Double,

    val severita: String,

    val data: Long
)