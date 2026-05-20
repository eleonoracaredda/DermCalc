package com.example.dermcalc_princ.database

import androidx.room.TypeConverter
import java.util.Date

// Classe di utility per Room per convertire tipi di dati non supportati nativamente (es. Date) in tipi supportati (es. Long/Timestamp)
class Converters {
    // Converte un timestamp Long letto dal database in un oggetto Date di Kotlin/Java
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    // Converte un oggetto Date in un timestamp Long per poterlo salvare nel database
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
