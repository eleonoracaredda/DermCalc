package Database

import Dominio.Misurazione
import Dominio.Pazienti
import Dominio.User
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        Pazienti::class,
        Misurazione::class
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun pazienteDao(): PazienteDao
    abstract fun misurazioneDao(): MisurazioneDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "dermcalc_database"
                    )
                        .fallbackToDestructiveMigration(false)
                        .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
