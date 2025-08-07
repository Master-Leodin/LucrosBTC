package btcemais.lucrosbtc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import btcemais.lucrosbtc.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.sobre_e_doa_es)
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed() // Don't know the new way to do this
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Don't know the new way to do this
        return true
    }
}