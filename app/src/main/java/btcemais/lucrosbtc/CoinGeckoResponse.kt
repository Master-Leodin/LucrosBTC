package btcemais.lucrosbtc

import com.google.gson.annotations.SerializedName

data class CoinGeckoResponse(
    @SerializedName("bitcoin")
    val bitcoin: BitcoinData
)

data class BitcoinData(
    @SerializedName("brl") val brl: Double,
    @SerializedName("usd") val usd: Double,
    @SerializedName("eur") val eur: Double,
    @SerializedName("last_updated_at") val lastUpdated: Long
)

data class LocalBitcoinsResponse(
    @SerializedName("data") val data: LocalBitcoinsData
)

data class LocalBitcoinsData(
    @SerializedName("rates") val rates: LocalBitcoinsRates,
    @SerializedName("updated_at") val updatedAt: String
)

data class LocalBitcoinsRates(
    @SerializedName("BRL") val brl: LocalBitcoinsRate,
    @SerializedName("USD") val usd: LocalBitcoinsRate,
    @SerializedName("EUR") val eur: LocalBitcoinsRate
)

data class LocalBitcoinsRate(
    @SerializedName("rate") val rate: String,
    @SerializedName("currency") val currency: String
)