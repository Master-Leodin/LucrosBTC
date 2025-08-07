package btcemais.lucrosbtc

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import btcemais.lucrosbtc.databinding.ActivityOverviewBinding
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class OverviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOverviewBinding
    private val savingsViewModel: SavingsViewModel by viewModels {
        SavingsViewModelFactory((application as SavingsApplication).database.savingsDao())
    }
    private val btcPriceViewModel: BtcPriceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        observeViewModels()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun observeViewModels() {
        lifecycleScope.launch {
            combine(
                savingsViewModel.brlSavings,
                savingsViewModel.usdSavings,
                savingsViewModel.eurSavings,
                btcPriceViewModel.btcPrices
            ) { brlSavings, usdSavings, eurSavings, btcPrices ->
                Quadruple(brlSavings, usdSavings, eurSavings, btcPrices)
            }.collect { (brlSavings, usdSavings, eurSavings, btcPrices) ->
                when (btcPrices) {
                    is BtcPriceUiState.Success -> {
                        updateCurrencyCards(
                            brlSavings,
                            usdSavings,
                            eurSavings,
                            btcPrices.brl,
                            btcPrices.usd,
                            btcPrices.eur
                        )
                    }
                    is BtcPriceUiState.Error -> {
                        Toast.makeText(
                            this@OverviewActivity,
                            btcPrices.message,
                            Toast.LENGTH_LONG
                        ).show()
                        updateCurrencyCards(brlSavings, usdSavings, eurSavings, 0.0, 0.0, 0.0)
                    }
                    BtcPriceUiState.Loading -> {
                    }
                }
            }
        }
    }

    private fun updateCurrencyCards(
        brlSavings: List<Savings>,
        usdSavings: List<Savings>,
        eurSavings: List<Savings>,
        brlPrice: Double,
        usdPrice: Double,
        eurPrice: Double
    ) {
        updateCurrencyCard(
            brlSavings,
            brlPrice,
            binding.totalBRLTextView,
            binding.btcBRLTextView,
            binding.satsBRLTextView,
            "R$"
        )

        updateCurrencyCard(
            usdSavings,
            usdPrice,
            binding.totalUSDTextView,
            binding.btcUSDTextView,
            binding.satsUSDTextView,
            "US$"
        )

        updateCurrencyCard(
            eurSavings,
            eurPrice,
            binding.totalEURTextView,
            binding.btcEURTextView,
            binding.satsEURTextView,
            "€"
        )
    }

    private fun updateCurrencyCard(
        savings: List<Savings>,
        btcPrice: Double,
        totalSpentTextView: android.widget.TextView,
        currentValueTextView: android.widget.TextView,
        totalSatsTextView: android.widget.TextView,
        currencySymbol: String
    ) {
        val totalSpent = savings.sumOf { it.amount }
        totalSpentTextView.text = "$currencySymbol ${"%.2f".format(totalSpent)}"

        val totalSats = savings.sumOf { it.sats }
        totalSatsTextView.text = "$totalSats sats"

        if (btcPrice > 0) {
            val btcAmount = totalSats.toDouble() / 100_000_000
            val currentValue = btcAmount * btcPrice
            currentValueTextView.text = "$currencySymbol ${"%.2f".format(currentValue)}"
        } else {
            currentValueTextView.text = "$currencySymbol 0,00"
        }
    }

    override fun onResume() {
        super.onResume()
        savingsViewModel.refreshData()
        btcPriceViewModel.fetchBtcPrices()
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)