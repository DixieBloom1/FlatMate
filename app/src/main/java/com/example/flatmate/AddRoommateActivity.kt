package com.example.flatmate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AddRoommateActivity : AppCompatActivity() {

    private lateinit var roommateIdEditText: EditText
    private lateinit var addRoommateButton: Button
    private lateinit var myIdTextView: TextView

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Store the current user's custom ID (stored in the "userId" field)
    private var myCustomUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_roommate)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        roommateIdEditText = findViewById(R.id.roommateIdEditText)
        addRoommateButton = findViewById(R.id.addRoommateButton)
        myIdTextView = findViewById(R.id.myIdTextView)

        val user = mAuth.currentUser
        if (user != null) {
            // Retrieve the current user's custom "userId"
            val userDocRef = db.collection("userProfiles").document(user.uid)
            userDocRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    myCustomUserId = document.getString("userId") ?: "Unknown"
                    myIdTextView.text = "My ID: $myCustomUserId"
                } else {
                    myIdTextView.text = "My ID: Unknown"
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load user data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }

        addRoommateButton.setOnClickListener {
            val roommateIdInput = roommateIdEditText.text.toString().trim()

            if (roommateIdInput.isEmpty()) {
                Toast.makeText(this, "Please enter a valid Roommate ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Prevent adding yourself by comparing the input to your custom ID
            if (roommateIdInput == myCustomUserId) {
                Toast.makeText(this, "You cannot add yourself as a roommate", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Query Firestore using the custom "userId" field rather than document ID
            db.collection("userProfiles")
                .whereEqualTo("userId", roommateIdInput)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        Toast.makeText(this, "Roommate with ID $roommateIdInput not found", Toast.LENGTH_SHORT).show()
                    } else {
                        // Roommate found; add to the current user's roommate list
                        val currentUser = mAuth.currentUser
                        if (currentUser != null) {
                            val currentUserDocRef = db.collection("userProfiles").document(currentUser.uid)
                            // Update the roommates array and increment the roommateCount field by 1
                            currentUserDocRef.update(
                                "roommates", FieldValue.arrayUnion(roommateIdInput),
                                "roommateCount", FieldValue.increment(1)
                            )
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Roommate with ID $roommateIdInput added successfully", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, HomeActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error updating roommates: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error fetching roommate data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
