package btcemais.lucrosbtc

import com.google.gson.annotations.SerializedName

data class KrakenResponse(
    @SerializedName("result") val result: Map<String, KrakenTicker>
)

data class KrakenTicker(
    @SerializedName("c") val lastTrade: List<String>
)