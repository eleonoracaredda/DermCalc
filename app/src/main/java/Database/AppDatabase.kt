package Database

import Dominio.Misurazione
import Dominio.Pazienti
import Dominio.User
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Definizione del database principale dell'applicazione tramite Room
@Database(
    // Elenco delle classi entità che compongono il database
    entities = [
        User::class,
        Pazienti::class,
        Misurazione::class
    ],
    // Versione dello schema del database (da incrementare in caso di modifiche alla struttura)
    version = 2
)
abstract class AppDatabase : RoomDatabase() {

    // Metodi astratti per ottenere i DAO (Data Access Objects) relativi a ciascuna entità
    abstract fun userDao(): UserDao
    abstract fun pazienteDao(): PazienteDao
    abstract fun misurazioneDao(): MisurazioneDao

    companion object {
        // Singleton del database per evitare l'apertura di più istanze costose
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Metodo per ottenere l'unica istanza del database
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Se l'istanza non esiste, viene creata utilizzando Room.databaseBuilder
                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "dermcalc_database"
                    )
                        // In caso di cambio versione senza migrazione definita, non cancella i dati (false)
                        .fallbackToDestructiveMigration(false)
                        .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
