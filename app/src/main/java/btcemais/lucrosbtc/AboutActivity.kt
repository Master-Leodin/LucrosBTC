package btcemais.lucrosbtc

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import btcemais.lucrosbtc.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    private val paypalPixEmail = "leonardo132@gmail.com"
    private val wiseId = "leonardot1427"
    private val wiseLink = "https://wise.com/pay/me/leonardot1427"
    private val lightningAddress = "mightynepal82@walletofsatoshi.com"
    private val bitcoinAddress = "bc1qjahmm3qtzpn9kc86j8uedejjuvtlp66z8w3wek"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.sobre_e_doa_es2)
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        setupClickListeners()
        setupAppInfo()
    }

    private fun setupAppInfo() {
        binding.tvAppVersion.text = getString(R.string.about_app_version, "1.0")
        binding.tvAppDescription.text = getString(R.string.about_app_description)
    }

    private fun setupClickListeners() {
        binding.btnCopiarEmail.setOnClickListener {
            copyToClipboard(paypalPixEmail, getString(R.string.success_email_copied))
        }

        binding.btnDoacaoWise.setOnClickListener {
            openExternalLink(wiseLink)
        }

        binding.btnCopiarWise.setOnClickListener {
            copyToClipboard(wiseId, getString(R.string.success_wise_id_copied))
        }

        binding.btnDoacaoLightning.setOnClickListener {
            copyToClipboard(lightningAddress, getString(R.string.success_lightning_copied))
        }

        binding.btnDoacaoBitcoin.setOnClickListener {
            copyToClipboard(bitcoinAddress, getString(R.string.success_bitcoin_copied))
        }

        binding.btnCompartilhar.setOnClickListener {
            shareApp()
        }
    }

    private fun openExternalLink(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, R.string.error_open_link, Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyToClipboard(text: String, successMessage: String) {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Donation", text)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, R.string.error_copy, Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.about_app_name))
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.share_app_text, "https://leonportfolio.netlify.app")
            )
            startActivity(Intent.createChooser(shareIntent, getString(R.string.about_share_app)))
        } catch (e: Exception) {
            Toast.makeText(this, R.string.error_share, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}