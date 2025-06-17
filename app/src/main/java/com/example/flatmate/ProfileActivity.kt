package com.example.flatmate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var userNameTextView: TextView
    private lateinit var userSurnameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var logoutButton: Button

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Povezivanje s UI elementima
        userNameTextView = findViewById(R.id.userNameTextView)
        userSurnameTextView = findViewById(R.id.userSurnameTextView)
        userEmailTextView = findViewById(R.id.userEmailTextView)
        editProfileButton = findViewById(R.id.editProfileButton)
        logoutButton = findViewById(R.id.logoutButton)

        // Dohvati korisničke podatke iz Firebase Authentication i Firestore
        val user = mAuth.currentUser
        if (user != null) {
            userEmailTextView.text = "Email: ${user.email}"
            val userDocRef = db.collection("users").document(user.uid)

            userDocRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val firstName = document.getString("firstName") ?: "Unknown"
                    val lastName = document.getString("lastName") ?: "Unknown"
                    userNameTextView.text = "Name: $firstName"
                    userSurnameTextView.text = "Surname: $lastName"
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting user data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigacija na Edit Profile ekran
        editProfileButton.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Odjava korisnika
        logoutButton.setOnClickListener {
            mAuth.signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Preusmjerenje na LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
