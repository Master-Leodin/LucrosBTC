package btcemais.lucrosbtc

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import btcemais.lucrosbtc.databinding.ActivitySplashBinding
import java.util.Locale

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val splashTimeOut: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.splashImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
        binding.splashText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))

        // Sempre mostra a seleção de idioma (não persiste a escolha)
        showLanguageSelection()
    }

    private fun showLanguageSelection() {
        // Mostra os elementos de seleção de idioma
        binding.languagePrompt.visibility = View.VISIBLE
        binding.languageSelector.visibility = View.VISIBLE

        // Configura os cliques nas bandeiras
        binding.flagPt.setOnClickListener { launchApp("pt") }
        binding.flagEn.setOnClickListener { launchApp("en") }
        binding.flagEs.setOnClickListener { launchApp("es") }
    }

    private fun launchApp(languageCode: String) {
        // Aplica o idioma temporariamente (apenas para esta sessão)
        val resources = resources
        val configuration = resources.configuration
        configuration.setLocale(Locale(languageCode))
        resources.updateConfiguration(configuration, resources.displayMetrics)

        // Inicia a HomeActivity após um pequeno delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, HomeActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 500) // Delay reduzido para melhor experiência
    }
}