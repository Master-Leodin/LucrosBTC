package btcemais.lucrosbtc

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Savings::class], version = 2)
abstract class SavingsDatabase : RoomDatabase() {
    abstract fun savingsDao(): SavingsDao

    companion object {
        @Volatile
        private var INSTANCE: SavingsDatabase? = null

        fun getDatabase(context: Context): SavingsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SavingsDatabase::class.java,
                    "savings_database"
                ).fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }}}