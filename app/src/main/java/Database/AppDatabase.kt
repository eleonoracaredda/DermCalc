package Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import Dominio.Misurazione
import Dominio.Pazienti
import Dominio.User

// Definizione del database principale dell'applicazione tramite Room
@Database(
    entities = [
        User::class,
        Pazienti::class,
        Misurazione::class
    ],
    // Aumenta questo numero SOLO se cambi la struttura delle tabelle (aggiungi colonne, etc.)
    version = 8, //aumentato da 7 a 8
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun pazienteDao(): PazienteDao
    abstract fun misurazioneDao(): MisurazioneDao

    companion object {
        // Migrazione dalla versione 7 alla 8: aggiunta colonna sesso
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                runCatching { db.execSQL("ALTER TABLE pazienti ADD COLUMN sesso TEXT NOT NULL DEFAULT 'M'") }
            }
        }

        // Gestione della migrazione dalla versione 6 alla 7: aggiunta tutti i campi mancanti
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Usiamo runCatching per evitare crash se alcune colonne sono già state create parzialmente
                runCatching { db.execSQL("ALTER TABLE pazienti ADD COLUMN caregiverNome TEXT") }
                runCatching { db.execSQL("ALTER TABLE pazienti ADD COLUMN caregiverTelefono TEXT") }
                runCatching { db.execSQL("ALTER TABLE pazienti ADD COLUMN terapia TEXT") }
                runCatching { db.execSQL("ALTER TABLE pazienti ADD COLUMN dataInizioTerapia INTEGER") }
                runCatching { db.execSQL("ALTER TABLE pazienti ADD COLUMN consenso INTEGER NOT NULL DEFAULT 0") }
                runCatching { db.execSQL("ALTER TABLE pazienti ADD COLUMN comorbilita TEXT") }
                
                // Aggiunta campo note alla tabella misurazioni (mancava!)
                runCatching { db.execSQL("ALTER TABLE misurazioni ADD COLUMN note TEXT") }
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
                    .addMigrations(MIGRATION_6_7, MIGRATION_7_8) // Registrazione delle migrazioni
                    .fallbackToDestructiveMigration(true) // Evita crash bloccanti se la migrazione fallisce
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
