package com.example.flatmate

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flatmate.models.Ad

class AdDetailActivity : AppCompatActivity() {

    private lateinit var adTitleTextView: TextView
    private lateinit var adNameTextView: TextView
    private lateinit var adDetailsTextView: TextView
    private lateinit var contactButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad_detail)

        // Inicijalizacija UI elemenata
        adTitleTextView = findViewById(R.id.adTitleTextView)
        adNameTextView = findViewById(R.id.adNameTextView)
        adDetailsTextView = findViewById(R.id.adDetailsTextView)
        contactButton = findViewById(R.id.contactButton)

        // Dohvati oglas iz Intenta
        val ad: Ad = intent.getParcelableExtra("adDetails")!!

        // Prikaz podataka
        adTitleTextView.text = ad.title
        adNameTextView.text = "Name: ${ad.name} ${ad.surname}"
        adDetailsTextView.text = "Faculty: ${ad.faculty}\nLocation: ${ad.location}\nDescription: ${ad.description}"

        // Contact gumb
        contactButton.setOnClickListener {
            Toast.makeText(this, "Contacting ${ad.name}...", Toast.LENGTH_SHORT).show()
        }
    }
}
