package btcemais.lucrosbtc

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings")
data class Savings(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val sats: Long,
    val currency: String,
    val description: String,
    val date: Long = System.currentTimeMillis()
)