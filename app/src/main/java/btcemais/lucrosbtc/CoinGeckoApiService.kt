package btcemais.lucrosbtc

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinGeckoApiService {
    @GET("simple/price")
    suspend fun getCoinGeckoPrices(
        @Query("ids") id: String = "bitcoin",
        @Query("vs_currencies") currencies: String = "brl,usd,eur",
        @Query("include_last_updated_at") includeUpdated: Boolean = true
    ): Response<CoinGeckoResponse>

    @GET("bitcoinaverage/ticker-all-currencies/")
    suspend fun getLocalBitcoinsPrices(): Response<LocalBitcoinsResponse>
}