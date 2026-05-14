package Dominio

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val taxCode: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)
