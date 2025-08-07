package btcemais.lucrosbtc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavingsViewModel(private val savingsDao: SavingsDao) : ViewModel() {

    val allSavings: Flow<List<Savings>> = savingsDao.getAllSavings()
        .map { list -> list.sortedByDescending { it.date } }

    val brlSavings: Flow<List<Savings>> = savingsDao.getSavingsByCurrency("brl")
    val usdSavings: Flow<List<Savings>> = savingsDao.getSavingsByCurrency("usd")
    val eurSavings: Flow<List<Savings>> = savingsDao.getSavingsByCurrency("eur")

    val uiState: StateFlow<SavingsUiState> = savingsDao.getAllSavings().map { savings ->
        SavingsUiState(
            savingsList = savings,
            totalSaved = savings.sumOf { it.amount },
            totalSats = savings.sumOf { it.sats }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SavingsUiState(emptyList(), 0.0, 0L)
    )

    fun addSavings(amount: Double, description: String, sats: Long, currency: String) {
        viewModelScope.launch {
            savingsDao.insert(
                Savings(
                    amount = amount,
                    description = description,
                    sats = sats,
                    currency = currency
                )
            )
        }
    }

    fun subtractSavings(amount: Double, description: String, sats: Long, currency: String) {
        viewModelScope.launch {
            savingsDao.insert(
                Savings(
                    amount = -amount,
                    description = "Subtract: $description",
                    sats = -sats,
                    currency = currency
                )
            )
        }
    }

    fun deleteSavings(savings: Savings) {
        viewModelScope.launch {
            savingsDao.delete(savings)
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            savingsDao.getAllSavings().collect {}
        }
    }
}

data class SavingsUiState(
    val savingsList: List<Savings>,
    val totalSaved: Double,
    val totalSats: Long
)