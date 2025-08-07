package btcemais.lucrosbtc

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsDao {
    @Insert
    suspend fun insert(savings: Savings)

    @Delete
    suspend fun delete(savings: Savings)

    @Query("SELECT * FROM savings ORDER BY date DESC")
    fun getAllSavings(): Flow<List<Savings>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM savings")
    fun getTotalSaved(): Flow<Double>

    @Query("SELECT COALESCE(SUM(sats), 0) FROM savings")
    fun getTotalSats(): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM savings WHERE currency = :currency")
    fun getTotalSaved(currency: String): Flow<Double>

    @Query("SELECT * FROM savings WHERE currency = :currency ORDER BY date DESC")
    fun getSavingsByCurrency(currency: String): Flow<List<Savings>>
}