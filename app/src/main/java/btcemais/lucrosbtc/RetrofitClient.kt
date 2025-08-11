package btcemais.lucrosbtc

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val COINGECKO_BASE_URL = "https://api.coingecko.com/api/v3/"
    private const val LOCALBITCOINS_BASE_URL = "https://localbitcoins.com/"
    private const val KRAKEN_BASE_URL = "https://api.kraken.com/0/"

    private fun createRetrofit(baseUrl: String): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val coinGeckoService: CoinGeckoApiService by lazy {
        createRetrofit(COINGECKO_BASE_URL).create(CoinGeckoApiService::class.java)
    }

    val localBitcoinsService: CoinGeckoApiService by lazy {
        createRetrofit(LOCALBITCOINS_BASE_URL).create(CoinGeckoApiService::class.java)
    }

    val krakenService: CoinGeckoApiService by lazy {
        createRetrofit(KRAKEN_BASE_URL).create(CoinGeckoApiService::class.java)
    }
}