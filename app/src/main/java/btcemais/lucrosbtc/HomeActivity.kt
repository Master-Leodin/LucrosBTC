package btcemais.lucrosbtc

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import btcemais.lucrosbtc.databinding.ActivityHomeBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val savingsViewModel: SavingsViewModel by viewModels {
        SavingsViewModelFactory((application as SavingsApplication).database.savingsDao())
    }
    private val btcPriceViewModel: BtcPriceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        setupClickListeners()
    }

    private fun setupActionBar() {
        supportActionBar?.title = getString(R.string.btc_seu_gerenciador)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun setupClickListeners() {
        binding.btnSavings.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.btnConversion.setOnClickListener {
            startActivity(Intent(this, ConversionActivity::class.java))
        }

        binding.btnBtcPrice.setOnClickListener {
            showBtcPriceDialog()
        }

        binding.btnOverview.setOnClickListener {
            savingsViewModel.refreshData()
            btcPriceViewModel.fetchBtcPrices()
            startActivity(Intent(this, OverviewActivity::class.java))
        }

        binding.btnAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }

    private fun showBtcPriceDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_btc_price, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("OK") { d, _ -> d.dismiss() }
            .create()

        val tvBrlPrice = dialogView.findViewById<TextView>(R.id.tvBrlPrice)
        val tvUsdPrice = dialogView.findViewById<TextView>(R.id.tvUsdPrice)
        val tvEurPrice = dialogView.findViewById<TextView>(R.id.tvEurPrice)
        val tvLastUpdated = dialogView.findViewById<TextView>(R.id.tvLastUpdated)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)
        val tvError = dialogView.findViewById<TextView>(R.id.tvError)
        val tvSource = dialogView.findViewById<TextView>(R.id.tvSource)

        lifecycleScope.launch {
            btcPriceViewModel.btcPrices.collect { state ->
                when (state) {
                    is BtcPriceUiState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        tvError.visibility = View.GONE
                        tvSource.visibility = View.GONE
                        tvBrlPrice.text = getString(R.string.carregando)
                        tvUsdPrice.text = ""
                        tvEurPrice.text = ""
                        tvLastUpdated.text = ""
                    }
                    is BtcPriceUiState.Success -> {
                        progressBar.visibility = View.GONE
                        tvError.visibility = View.GONE
                        tvSource.visibility = View.VISIBLE

                        tvBrlPrice.text = "R$ ${"%.2f".format(state.brl)}"
                        tvUsdPrice.text = "US$ ${"%.2f".format(state.usd)}"
                        tvEurPrice.text = "€ ${"%.2f".format(state.eur)}"
                        tvLastUpdated.text = getString(R.string.atualizado_em, state.lastUpdated)
                        tvSource.text = getString(R.string.fonte_dados, state.source)

                        Toast.makeText(
                            this@HomeActivity,
                            getString(R.string.dados_obtidos_de, state.source),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is BtcPriceUiState.Error -> {
                        progressBar.visibility = View.GONE
                        tvSource.visibility = View.GONE
                        tvError.text = state.message
                        tvError.visibility = View.VISIBLE
                        tvBrlPrice.text = "-"
                        tvUsdPrice.text = "-"
                        tvEurPrice.text = "-"
                        tvLastUpdated.text = ""

                        Toast.makeText(
                            this@HomeActivity,
                            state.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        dialog.setOnShowListener {
            btcPriceViewModel.fetchBtcPrices()
        }

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        savingsViewModel.refreshData()
    }
}