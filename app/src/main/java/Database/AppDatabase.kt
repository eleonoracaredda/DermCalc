package Database

import Dominio.Misurazione
import Dominio.Pazienti
import Dominio.User
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// Definizione del database principale dell'applicazione tramite Room
@Database(
    entities = [
        User::class,
        Pazienti::class,
        Misurazione::class
    ],
    // Aumenta questo numero SOLO se cambi la struttura delle tabelle (aggiungi colonne, etc.)
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun pazienteDao(): PazienteDao
    abstract fun misurazioneDao(): MisurazioneDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dermcalc_database"
                )
                // Se aumenti la versione e non vuoi perdere i dati, devi usare .addMigrations()
                // .fallbackToDestructiveMigration(false) impedisce la cancellazione automatica,
                // ma farà crashare l'app se la versione non coincide e manca una migrazione.
                // Questo è utile per accorgersi dell'errore durante lo sviluppo.
                .fallbackToDestructiveMigration(false) 
                .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
