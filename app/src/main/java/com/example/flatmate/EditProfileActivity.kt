package com.example.flatmate

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EditProfileActivity : AppCompatActivity() {

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var facultyEditText: EditText
    private lateinit var birthYearEditText: EditText
    private lateinit var saveChangesButton: Button
    private lateinit var changePasswordButton: Button

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        firstNameEditText = findViewById(R.id.firstNameEditText)
        lastNameEditText = findViewById(R.id.lastNameEditText)
        facultyEditText = findViewById(R.id.facultyEditText)
        birthYearEditText = findViewById(R.id.birthYearEditText)
        saveChangesButton = findViewById(R.id.saveChangesButton)
        changePasswordButton = findViewById(R.id.changePasswordButton)

        val user = mAuth.currentUser
        if (user != null) {
            val userDocRef = db.collection("userProfiles").document(user.uid)

            userDocRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val faculty = document.getString("faculty") ?: ""
                    val birthYear = document.getString("birthYear") ?: ""

                    firstNameEditText.setText(firstName)
                    lastNameEditText.setText(lastName)
                    facultyEditText.setText(faculty)
                    birthYearEditText.setText(birthYear)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error getting user profile data", Toast.LENGTH_SHORT).show()
            }
        }

        saveChangesButton.setOnClickListener {
            val newFirstName = firstNameEditText.text.toString()
            val newLastName = lastNameEditText.text.toString()
            val newFaculty = facultyEditText.text.toString()
            val newBirthYear = birthYearEditText.text.toString()

            if (newFirstName.isNotEmpty() && newLastName.isNotEmpty() && newFaculty.isNotEmpty() && newBirthYear.isNotEmpty()) {
                val user = mAuth.currentUser
                if (user != null) {
                    val userProfileDocRef = db.collection("userProfiles").document(user.uid)

                    val profileData = hashMapOf(
                        "firstName" to newFirstName,
                        "lastName" to newLastName,
                        "faculty" to newFaculty,
                        "birthYear" to newBirthYear
                    )

                    // Use set() with merge to avoid overwriting userId
                    userProfileDocRef.set(profileData, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }

        changePasswordButton.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }
    }
}

