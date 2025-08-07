package btcemais.lucrosbtc

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import btcemais.lucrosbtc.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val savingsViewModel: SavingsViewModel by viewModels {
        SavingsViewModelFactory((application as SavingsApplication).database.savingsDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed() // Don't know the new way to do this
        }
    }

    private fun setupRecyclerView() {
        binding.savingsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.savingsRecyclerView.adapter = SavingsAdapter { savings ->
            showDeleteConfirmation(savings)
        }
    }

    private fun setupClickListeners() {
        binding.addButton.setOnClickListener {
            addNewSavings()
        }

        binding.subtractButton.setOnClickListener {
            subtractSavings()
        }

        binding.descriptionEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addNewSavings()
                true
            } else {
                false
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                savingsViewModel.allSavings.collect { savingsList ->
                    Log.d("MainActivity", "Total de itens recebidos: ${savingsList.size}")
                    updateSavingsList(savingsList)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateSavingsList(savingsList: List<Savings>) {
        Log.d("MainActivity", "Atualizando lista com ${savingsList.size} itens")
        (binding.savingsRecyclerView.adapter as SavingsAdapter).submitList(savingsList)
        binding.emptyView.visibility = if (savingsList.isEmpty()) View.VISIBLE else View.GONE
        binding.savingsRecyclerView.visibility = if (savingsList.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun addNewSavings() {
        val amountText = binding.amountEditText.text.toString()
        val satsText = binding.satsEditText.text.toString()
        val description = binding.descriptionEditText.text.toString()
        val selectedPosition = binding.currencySpinner.selectedItemPosition

        if (amountText.isBlank() || satsText.isBlank() || description.isBlank()) {
            showError(getString(R.string.preencha_todos_os_campos))
            return
        }

        val currency = when(selectedPosition) {
            0 -> "brl"
            1 -> "usd"
            2 -> "eur"
            else -> "brl"
        }

        val amount = amountText.toDoubleOrNull() ?: run {
            showError(getString(R.string.valor_inv_lido))
            return
        }

        val sats = satsText.toLongOrNull() ?: run {
            showError(getString(R.string.valor_em_satoshis_inv_lido))
            return
        }

        savingsViewModel.addSavings(amount, description, sats, currency)
        clearInputs()
    }

    private fun subtractSavings() {
        val amountText = binding.amountEditText.text.toString()
        val satsText = binding.satsEditText.text.toString()
        val description = binding.descriptionEditText.text.toString()
        val selectedPosition = binding.currencySpinner.selectedItemPosition

        if (amountText.isBlank() || satsText.isBlank() || description.isBlank()) {
            showError(getString(R.string.preencha_todos_os_campos))
            return
        }

        val currency = when(selectedPosition) {
            0 -> "brl"
            1 -> "usd"
            2 -> "eur"
            else -> "brl"
        }

        val amount = amountText.toDoubleOrNull() ?: run {
            showError(getString(R.string.valor_inv_lido))
            return
        }

        val sats = satsText.toLongOrNull() ?: run {
            showError(getString(R.string.valor_em_satoshis_inv_lido))
            return
        }

        savingsViewModel.subtractSavings(amount, description, sats, currency)
        clearInputs()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun clearInputs() {
        binding.amountEditText.text?.clear()
        binding.satsEditText.text?.clear()
        binding.descriptionEditText.text?.clear()
        binding.amountEditText.requestFocus()
    }

    private fun showDeleteConfirmation(savings: Savings) {
        val currencySymbol = when(savings.currency) {
            "usd" -> "US$"
            "eur" -> "€"
            else -> "R$"
        }
        val amountFormatted = "%.2f".format(abs(savings.amount))

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirmar_exclus_o))
            .setMessage("${getString(R.string.deseja_remover_esta_transa_o)}\n$currencySymbol $amountFormatted - ${savings.description}")
            .setPositiveButton(getString(R.string.sim)) { _, _ ->
                savingsViewModel.deleteSavings(savings)
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        savingsViewModel.refreshData()
    }
}