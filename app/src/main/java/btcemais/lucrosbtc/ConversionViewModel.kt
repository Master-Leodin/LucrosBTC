package btcemais.lucrosbtc

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException

class ConversionViewModel : ViewModel() {
    private val coinGeckoService = RetrofitClient.coinGeckoService
    private val localBitcoinsService = RetrofitClient.localBitcoinsService

    private val _conversionResult = MutableStateFlow<String?>(null)
    val conversionResult: StateFlow<String?> = _conversionResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun convert(amount: Double, isCurrencyToBtc: Boolean, currency: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _conversionResult.value = null

                try {
                    val (rate, symbol) = withTimeout(5000) {
                        getExchangeRateFromCoinGecko(currency)
                    }
                    performConversion(amount, isCurrencyToBtc, currency, rate, symbol)
                    return@launch
                } catch (e: TimeoutCancellationException) {
                    Log.w("ConversionVM", "CoinGecko timeout, trying LocalBitcoins")
                } catch (e: Exception) {
                    Log.w("ConversionVM", "CoinGecko error: ${e.message}")
                }

                try {
                    val (rate, symbol) = getExchangeRateFromLocalBitcoins(currency)
                    performConversion(amount, isCurrencyToBtc, currency, rate, symbol)
                } catch (e: Exception) {
                    _error.value = "Erro: Ambas APIs falharam - ${e.message ?: "Erro desconhecido"}"
                    Log.e("ConversionVM", "LocalBitcoins error", e)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun getExchangeRateFromCoinGecko(currency: String): Pair<Double, String> {
        val response = withContext(Dispatchers.IO) {
            coinGeckoService.getCoinGeckoPrices()
        }

        if (!response.isSuccessful || response.body() == null) {
            throw IOException("API error: ${response.code()}")
        }

        return when (currency) {
            "usd" -> response.body()!!.bitcoin.usd to "US$"
            "eur" -> response.body()!!.bitcoin.eur to "€"
            else -> response.body()!!.bitcoin.brl to "R$"
        }
    }

    private suspend fun getExchangeRateFromLocalBitcoins(currency: String): Pair<Double, String> {
        val response = withContext(Dispatchers.IO) {
            localBitcoinsService.getLocalBitcoinsPrices()
        }

        if (!response.isSuccessful || response.body() == null) {
            throw IOException("API error: ${response.code()}")
        }

        return when (currency) {
            "usd" -> response.body()!!.data.rates.usd.rate.toDouble() to "US$"
            "eur" -> response.body()!!.data.rates.eur.rate.toDouble() to "€"
            else -> response.body()!!.data.rates.brl.rate.toDouble() to "R$"
        }
    }

    private fun performConversion(
        amount: Double,
        isCurrencyToBtc: Boolean,
        currency: String,
        rate: Double,
        symbol: String
    ) {
        if (rate <= 0) throw IllegalArgumentException("Taxa de câmbio inválida")

        val result = if (isCurrencyToBtc) {
            val btcAmount = amount / rate
            "$symbol ${"%.2f".format(amount)} = ${"%.8f".format(btcAmount)} BTC"
        } else {
            val currencyAmount = amount * rate
            "${"%.8f".format(amount)} BTC = $symbol ${"%.2f".format(currencyAmount)}"
        }

        _conversionResult.value = result
    }
}