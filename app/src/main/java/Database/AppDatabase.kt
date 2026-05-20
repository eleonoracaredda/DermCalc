package Database

import Dominio.Misurazione
import Dominio.Pazienti
import Dominio.User
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Definizione del database principale dell'applicazione tramite Room
@Database(
    entities = [
        User::class,
        Pazienti::class,
        Misurazione::class
    ],
    // Aumenta questo numero SOLO se cambi la struttura delle tabelle (aggiungi colonne, etc.)
    version = 7, //aumentato da 6 a 7
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun pazienteDao(): PazienteDao
    abstract fun misurazioneDao(): MisurazioneDao

    companion object {
        // Gestione della migrazione dalla versione 6 alla 7: aggiunta campi per il caregiver
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE pazienti ADD COLUMN caregiverNome TEXT")
                database.execSQL("ALTER TABLE pazienti ADD COLUMN caregiverTelefono TEXT")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Pattern Singleton per garantire un'unica istanza del database in tutta l'app
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dermcalc_database"
                )
                    .addMigrations(MIGRATION_6_7) // Registrazione delle migrazioni
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
