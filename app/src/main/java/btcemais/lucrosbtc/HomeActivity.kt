package btcemais.lucrosbtc

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import btcemais.lucrosbtc.databinding.ActivityHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val savingsViewModel: SavingsViewModel by viewModels {
        SavingsViewModelFactory((application as SavingsApplication).database.savingsDao())
    }
    private val btcPriceViewModel: BtcPriceViewModel by viewModels()

    private var updateApkFile: File? = null
    private var updateDialog: AlertDialog? = null
    private val TAG = "HomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "HomeActivity created")

        setupActionBar()
        setupClickListeners()

        // Check for updates after a small delay for UI to load
        lifecycleScope.launch {
            delay(2000) // 2 seconds delay
            checkForAppUpdate()
        }
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

    private fun checkForAppUpdate() {
        Log.d(TAG, "Checking for app update...")
        lifecycleScope.launch {
            try {
                val updateInfo = VersionChecker.checkForUpdate(this@HomeActivity)
                Log.d(TAG, "Update check completed: $updateInfo")

                if (updateInfo != null) {
                    Log.d(TAG, "Update available! Showing dialog...")
                    showUpdateDialog(updateInfo)
                } else {
                    Log.d(TAG, "No update available or check failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during update check: ${e.message}", e)
            }
        }
    }

    private fun showUpdateDialog(version: AppVersion) {
        runOnUiThread {
            try {
                Log.d(TAG, "Inflating update dialog...")
                val dialogView = layoutInflater.inflate(R.layout.dialog_update_available, null)
                val releaseNotesTextView = dialogView.findViewById<TextView>(R.id.releaseNotesTextView)
                val updateButton = dialogView.findViewById<Button>(R.id.updateButton)
                val laterButton = dialogView.findViewById<Button>(R.id.laterButton)
                val progressBar = dialogView.findViewById<ProgressBar>(R.id.updateProgressBar)

                val dialog = AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create()

                updateDialog = dialog

                releaseNotesTextView.text = version.releaseNotes

                updateButton.setOnClickListener {
                    Log.d(TAG, "Update button clicked for URL: ${version.apkUrl}")
                    startUpdateDownload(version.apkUrl, dialogView)
                }

                laterButton.setOnClickListener {
                    Log.d(TAG, "Later button clicked")
                    dialog.dismiss()
                    updateDialog = null
                }

                dialog.show()
                Log.d(TAG, "Update dialog shown successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error showing update dialog: ${e.message}", e)
                Toast.makeText(this, R.string.update_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startUpdateDownload(apkUrl: String, dialogView: View) {
        val updateButton = dialogView.findViewById<Button>(R.id.updateButton)
        val laterButton = dialogView.findViewById<Button>(R.id.laterButton)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.updateProgressBar)

        updateButton.isEnabled = false
        laterButton.isEnabled = false
        updateButton.text = getString(R.string.downloading)
        progressBar.visibility = View.VISIBLE
        progressBar.progress = 0

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting download from: $apkUrl")
                val url = URL(apkUrl)
                val connection = url.openConnection() as HttpsURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.connect()

                val contentLength = connection.contentLength
                Log.d(TAG, "Content length: $contentLength")
                val inputStream = connection.inputStream

                val apkFile = File(cacheDir, "btc_profits_update.apk")
                val outputStream = FileOutputStream(apkFile)

                var totalBytesRead = 0
                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int

                inputStream.use { input ->
                    outputStream.use { output ->
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead

                            if (contentLength > 0) {
                                val progress = (totalBytesRead * 100 / contentLength).toInt()
                                if (progress % 10 == 0) {
                                    Log.d(TAG, "Download progress: $progress%")
                                }
                                withContext(Dispatchers.Main) {
                                    progressBar.progress = progress
                                }
                            }
                        }
                    }
                }

                updateApkFile = apkFile
                Log.d(TAG, "Download completed, file size: ${apkFile.length()} bytes")

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    updateButton.text = getString(R.string.install)
                    updateButton.isEnabled = true
                    laterButton.isEnabled = true

                    triggerUpdateInstallation(apkFile)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Download error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    updateButton.text = getString(R.string.try_again)
                    updateButton.isEnabled = true
                    laterButton.isEnabled = true

                    Toast.makeText(
                        this@HomeActivity,
                        getString(R.string.download_error, e.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun triggerUpdateInstallation(apkFile: File) {
        try {
            Log.d(TAG, "Triggering installation for: ${apkFile.absolutePath}")

            // Check if file exists and has adequate size
            if (!apkFile.exists() || apkFile.length() == 0L) {
                Toast.makeText(this, R.string.corrupted_update_file, Toast.LENGTH_SHORT).show()
                return
            }

            val apkUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(this, "${packageName}.fileprovider", apkFile)
            } else {
                Uri.fromFile(apkFile)
            }

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            startActivity(installIntent)
            updateDialog?.dismiss()
            updateDialog = null
            Log.d(TAG, "Installation intent started successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Installation error: ${e.message}", e)

            // More specific message
            val errorMsg = when {
                e.message?.contains("conflict") == true ->
                    getString(R.string.package_conflict)
                e.message?.contains("no app") == true ->
                    getString(R.string.no_app_can_open_file)
                else -> getString(R.string.installation_error, e.message)
            }

            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
        }
    }

    // ... o resto do código da HomeActivity (showBtcPriceDialog, onResume, etc.) permanece igual
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

    override fun onDestroy() {
        updateApkFile?.delete()
        updateDialog?.dismiss()
        super.onDestroy()
    }
}