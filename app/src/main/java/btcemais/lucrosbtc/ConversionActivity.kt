package btcemais.lucrosbtc

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import btcemais.lucrosbtc.databinding.ActivityConversionBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ConversionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConversionBinding
    private val viewModel: ConversionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        setupSpinner()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupActionBar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed() // Don't know the new way to do this
        }
    }

    private fun setupSpinner() {
        binding.conversionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateInputHint(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateInputHint(selectedPosition: Int) {
        val hint = when(selectedPosition) {
            0 -> getString(R.string.valor_em_reais)    // Real → BTC
            1 -> getString(R.string.valor_em_btc)      // BTC → Real
            2 -> getString(R.string.valor_em_dolares)  // Dólar → BTC
            3 -> getString(R.string.valor_em_btc)      // BTC → Dólar
            4 -> getString(R.string.valor_em_euros)    // Euro → BTC
            5 -> getString(R.string.valor_em_btc)      // BTC → Euro
            else -> getString(R.string.valor_em_reais) // Padrão
        }
        binding.inputLayout.hint = hint
    }

    private fun setupClickListeners() {
        binding.convertButton.setOnClickListener {
            val selectedPosition = binding.conversionSpinner.selectedItemPosition
            val amountText = binding.amountEditText.text.toString()

            if (amountText.isBlank()) {
                Toast.makeText(this, getString(R.string.digite_um_valor), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull() ?: run {
                Toast.makeText(this, getString(R.string.valor_inv_lido), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val (isCurrencyToBtc, currency) = when(selectedPosition) {
                0 -> Pair(true, "brl")   // Real → BTC
                1 -> Pair(false, "brl")  // BTC → Real
                2 -> Pair(true, "usd")   // Dólar → BTC
                3 -> Pair(false, "usd")  // BTC → Dólar
                4 -> Pair(true, "eur")   // Euro → BTC
                5 -> Pair(false, "eur")   // BTC → Euro
                else -> Pair(true, "brl") // Padrão
            }

            viewModel.convert(amount, isCurrencyToBtc, currency)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            launch {
                viewModel.isLoading.collect { isLoading ->
                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    binding.convertButton.isEnabled = !isLoading
                    if (isLoading) {
                        binding.errorTextView.text = ""
                        binding.resultTextView.text = ""
                    }
                }
            }

            launch {
                viewModel.conversionResult.collect { result ->
                    result?.let {
                        binding.resultTextView.text = it
                        binding.errorTextView.text = ""
                    }
                }
            }

            launch {
                viewModel.error.collect { error ->
                    error?.let {
                        binding.errorTextView.text = it
                        binding.resultTextView.text = ""
                        Toast.makeText(this@ConversionActivity, it, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Don't know the new way to do this
        return true
    }
}