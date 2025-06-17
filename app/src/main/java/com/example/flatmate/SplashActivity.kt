package com.example.flatmate

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Postavljanje vremena trajanja Splash Screena (1-2 sekunde)
        Handler().postDelayed({
            // Nakon 2 sekunde, otvara LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()  // Zatvaranje Splash Activity
        }, 2000)
    }
}
