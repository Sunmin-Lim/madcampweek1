package com.example.madcamp1

import android.os.*
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.example.madcamp1.MainActivity
import com.example.madcamp1.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.logoImageView)
        val rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate)
        logo.startAnimation(rotateAnim)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000) // 3초 후 메인화면으로 이동
    }
}
