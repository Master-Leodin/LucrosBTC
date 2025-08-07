package btcemais.lucrosbtc

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import btcemais.lucrosbtc.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val splashTimeOut: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.splashImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
        binding.splashText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, HomeActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out) // Don't know the new way to do this
            finish()
        }, splashTimeOut)
    }

}