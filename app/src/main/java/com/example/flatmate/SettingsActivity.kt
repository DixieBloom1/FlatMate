package com.example.flatmate

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class SettingsActivity : AppCompatActivity() {

    private lateinit var logoutButton: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        mAuth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)

        logoutButton = findViewById(R.id.logoutButton)

        // Funkcija za odjavu korisnika
        logoutButton.setOnClickListener {
            mAuth.signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Preusmjerenje na LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()  // Zatvori ovu aktivnost
        }

    }
}
