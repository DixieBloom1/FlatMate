package com.example.flatmate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class HomeActivity : AppCompatActivity() {

    private lateinit var userNameTextView: TextView
    private lateinit var roommateCountTextView: TextView
    private lateinit var userIdTextView: TextView
    private lateinit var addRoommateButton: Button
    private lateinit var searchRoommatesButton: Button
    private lateinit var manageExpensesButton: Button
    private lateinit var viewRoommatesButton: Button
    private lateinit var settingsButton: Button
    private lateinit var editProfileButton: Button

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Firestore listener reference
    private var userDocListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Link UI elements
        userNameTextView = findViewById(R.id.userNameTextView)
        roommateCountTextView = findViewById(R.id.roommateCountTextView)
        userIdTextView = findViewById(R.id.userIdTextView)
        addRoommateButton = findViewById(R.id.addRoommateButton)
        searchRoommatesButton = findViewById(R.id.searchRoommatesButton)
        manageExpensesButton = findViewById(R.id.manageExpensesButton)
        viewRoommatesButton = findViewById(R.id.viewRoommatesButton)
        settingsButton = findViewById(R.id.settingsButton)
        editProfileButton = findViewById(R.id.editProfileButton)

        // Set up the user snapshot listener
        val user = mAuth.currentUser
        if (user != null) {
            val userDocRef = db.collection("userProfiles").document(user.uid)
            userDocListener = userDocRef.addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error fetching user data: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if ((documentSnapshot != null) && documentSnapshot.exists()) {
                    val firstName = documentSnapshot.getString("firstName") ?: "User"
                    val userId = documentSnapshot.getString("userId") ?: "Unknown"
                    val roommateCount = documentSnapshot.getLong("roommateCount") ?: 0

                    userNameTextView.text = "Hello, $firstName"
                    roommateCountTextView.text = "Roommate count: $roommateCount"
                    userIdTextView.text = "My ID: $userId"
                } else {
                    Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }

        // Navigation button listeners
        addRoommateButton.setOnClickListener {
            startActivity(Intent(this, AddRoommateActivity::class.java))
        }
        searchRoommatesButton.setOnClickListener {
            startActivity(Intent(this, SearchRoommatesActivity::class.java))
        }
        viewRoommatesButton.setOnClickListener {
            startActivity(Intent(this, RoommateListActivity::class.java))
        }
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        manageExpensesButton.setOnClickListener {
            startActivity(Intent(this, ManageExpensesActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        userDocListener?.remove()
    }
}
