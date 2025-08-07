package btcemais.lucrosbtc

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.*

class BtcPriceViewModel : ViewModel() {
    private val coinGeckoService = RetrofitClient.coinGeckoService
    private val localBitcoinsService = RetrofitClient.localBitcoinsService
    private val originalBuyPrice = 200000.00

    private val _btcPrices = MutableStateFlow<BtcPriceUiState>(BtcPriceUiState.Loading)
    val btcPrices: StateFlow<BtcPriceUiState> = _btcPrices

    fun fetchBtcPrices() {
        viewModelScope.launch {
            _btcPrices.value = BtcPriceUiState.Loading

            try {
                try {
                    val coinGeckoData = withTimeout(5000) {
                        fetchFromCoinGecko()
                    }
                    _btcPrices.value = coinGeckoData
                    return@launch
                } catch (e: TimeoutCancellationException) {
                    Log.w("BtcPriceVM", "CoinGecko timeout, trying LocalBitcoins")
                } catch (e: Exception) {
                    Log.w("BtcPriceVM", "CoinGecko error: ${e.message}", e)
                }

                try {
                    val localBitcoinsData = fetchFromLocalBitcoins()
                    _btcPrices.value = localBitcoinsData
                } catch (e: Exception) {
                    Log.e("BtcPriceVM", "LocalBitcoins error: ${e.message}", e)
                    _btcPrices.value = BtcPriceUiState.Error(
                        "Ambas APIs falharam: ${e.message ?: "Erro desconhecido"}"
                    )
                }
            } catch (e: Exception) {
                Log.e("BtcPriceVM", "Error fetching BTC prices", e)
                _btcPrices.value = BtcPriceUiState.Error(
                    "Erro ao buscar preços: ${e.message ?: "Erro desconhecido"}"
                )
            }
        }
    }

    private suspend fun fetchFromCoinGecko(): BtcPriceUiState {
        val response = coinGeckoService.getCoinGeckoPrices()

        if (!response.isSuccessful || response.body() == null) {
            throw Exception("CoinGecko API error: ${response.code()}")
        }

        val data = response.body()!!
        return BtcPriceUiState.Success(
            brl = data.bitcoin.brl,
            usd = data.bitcoin.usd,
            eur = data.bitcoin.eur,
            originalBuyPrice = originalBuyPrice,
            lastUpdated = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(Date(data.bitcoin.lastUpdated * 1000)),
            source = "CoinGecko"
        )
    }

    private suspend fun fetchFromLocalBitcoins(): BtcPriceUiState {
        val response = localBitcoinsService.getLocalBitcoinsPrices()

        if (!response.isSuccessful || response.body() == null) {
            throw Exception("LocalBitcoins API error: ${response.code()}")
        }

        val data = response.body()!!
        return BtcPriceUiState.Success(
            brl = data.data.rates.brl.rate.toDouble(),
            usd = data.data.rates.usd.rate.toDouble(),
            eur = data.data.rates.eur.rate.toDouble(),
            originalBuyPrice = originalBuyPrice,
            lastUpdated = data.data.updatedAt,
            source = "LocalBitcoins"
        )
    }
}

sealed class BtcPriceUiState {
    object Loading : BtcPriceUiState()
    data class Success(
        val brl: Double,
        val usd: Double,
        val eur: Double,
        val originalBuyPrice: Double,
        val lastUpdated: String,
        val source: String = "CoinGecko"
    ) : BtcPriceUiState()
    data class Error(val message: String) : BtcPriceUiState()
}