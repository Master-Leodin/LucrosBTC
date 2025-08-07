package btcemais.lucrosbtc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SavingsViewModelFactory(private val savingsDao: SavingsDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SavingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SavingsViewModel(savingsDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}