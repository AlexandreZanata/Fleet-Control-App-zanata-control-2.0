package com.alexandre.controledefrota

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIMEOUT: Long = 2500 // 2.5 segundos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Carregar animações
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        // Aplicar animações
        val cardViewLogo = findViewById<CardView>(R.id.cardViewLogo)
        val txtAppName = findViewById<TextView>(R.id.txtAppName)
        val progressBar = findViewById<View>(R.id.progressBar)
        val txtLoading = findViewById<TextView>(R.id.txtLoading)
        val txtCopyright = findViewById<TextView>(R.id.txtCopyright)
        val txtVersion = findViewById<TextView>(R.id.txtVersion)

        cardViewLogo.startAnimation(fadeIn)
        txtAppName.startAnimation(slideUp)

        // Iniciar animações com delay
        Handler(Looper.getMainLooper()).postDelayed({
            progressBar.visibility = View.VISIBLE
            txtLoading.visibility = View.VISIBLE
            progressBar.startAnimation(fadeIn)
            txtLoading.startAnimation(fadeIn)
        }, 300)

        Handler(Looper.getMainLooper()).postDelayed({
            txtCopyright.visibility = View.VISIBLE
            txtVersion.visibility = View.VISIBLE
            txtCopyright.startAnimation(fadeIn)
            txtVersion.startAnimation(fadeIn)
        }, 600)

        // Opcional: Exibir a versão atual do aplicativo
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val versionName = packageInfo.versionName
            txtVersion.text = "Versão $versionName"
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Aguarda o tempo definido e inicia a MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, SPLASH_TIMEOUT)
    }
}