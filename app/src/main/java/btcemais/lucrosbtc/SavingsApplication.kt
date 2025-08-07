package btcemais.lucrosbtc

import android.app.Application

class SavingsApplication : Application() {
    val database: SavingsDatabase by lazy { SavingsDatabase.getDatabase(this) }
}