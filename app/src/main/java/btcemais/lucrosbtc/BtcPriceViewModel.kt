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
                } catch (e: Exception) {
                    Log.w("BtcPriceVM", "CoinGecko error", e)
                }

                try {
                    val localBitcoinsData = fetchFromLocalBitcoins()
                    _btcPrices.value = localBitcoinsData
                    return@launch
                } catch (e: Exception) {
                    Log.w("BtcPriceVM", "LocalBitcoins error", e)
                }

                try {
                    val krakenData = fetchFromKraken()
                    _btcPrices.value = krakenData
                } catch (e: Exception) {
                    _btcPrices.value = BtcPriceUiState.Error("All APIs failed")
                }
            } catch (e: Exception) {
                _btcPrices.value = BtcPriceUiState.Error("Error fetching prices")
            }
        }
    }

    private suspend fun fetchFromCoinGecko(): BtcPriceUiState {
        val response = RetrofitClient.coinGeckoService.getCoinGeckoPrices()
        if (!response.isSuccessful || response.body() == null) {
            throw Exception("CoinGecko API error")
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
        val response = RetrofitClient.localBitcoinsService.getLocalBitcoinsPrices()
        if (!response.isSuccessful || response.body() == null) {
            throw Exception("LocalBitcoins API error")
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

    private suspend fun fetchFromKraken(): BtcPriceUiState {
        val response = RetrofitClient.krakenService.getKrakenPrices()
        if (!response.isSuccessful || response.body() == null) {
            throw Exception("Kraken API error")
        }
        val data = response.body()!!.result
        return BtcPriceUiState.Success(
            brl = data["XBTBRL"]?.lastTrade?.first()?.toDoubleOrNull() ?: 0.0,
            usd = data["XBTUSD"]?.lastTrade?.first()?.toDoubleOrNull() ?: 0.0,
            eur = data["XBTEUR"]?.lastTrade?.first()?.toDoubleOrNull() ?: 0.0,
            originalBuyPrice = originalBuyPrice,
            lastUpdated = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(Date()),
            source = "Kraken"
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
        val source: String
    ) : BtcPriceUiState()
    data class Error(val message: String) : BtcPriceUiState()
}