package com.example.flatmate

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class NewAdActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var surnameEditText: EditText
    private lateinit var birthYearEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var facultyEditText: EditText
    private lateinit var locationEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var saveAdButton: Button

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_ad)

        db = FirebaseFirestore.getInstance()

        titleEditText = findViewById(R.id.titleEditText)
        nameEditText = findViewById(R.id.nameEditText)
        surnameEditText = findViewById(R.id.surnameEditText)
        birthYearEditText = findViewById(R.id.birthYearEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        facultyEditText = findViewById(R.id.facultyEditText)
        locationEditText = findViewById(R.id.locationEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        saveAdButton = findViewById(R.id.saveAdButton)

        saveAdButton.setOnClickListener {
            saveNewAd()
        }
    }

    private fun saveNewAd() {
        val title = titleEditText.text.toString()
        val name = nameEditText.text.toString()
        val surname = surnameEditText.text.toString()
        val birthYear = birthYearEditText.text.toString()
        val phone = phoneEditText.text.toString()
        val faculty = facultyEditText.text.toString()
        val location = locationEditText.text.toString()
        val description = descriptionEditText.text.toString()

        if (title.isNotEmpty() && name.isNotEmpty() && surname.isNotEmpty() && birthYear.isNotEmpty() && phone.isNotEmpty() && faculty.isNotEmpty() && location.isNotEmpty() && description.isNotEmpty()) {
            val ad = hashMapOf(
                "title" to title,
                "name" to name,
                "surname" to surname,
                "birthYear" to birthYear,
                "phone" to phone,
                "faculty" to faculty,
                "location" to location,
                "description" to description
            )

            db.collection("ads")
                .add(ad)
                .addOnSuccessListener {
                    Toast.makeText(this, "Ad added successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error adding ad: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
        }
    }
}
