package com.example.flatmate

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        nameEditText = findViewById(R.id.nameEditText)
        lastNameEditText = findViewById(R.id.lastNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
    }

    private fun generateUserId(): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = Random()
        val sb = StringBuilder()
        for (i in 0 until 10) {
            val randomChar = chars[random.nextInt(chars.length)]
            sb.append(randomChar)
        }
        return sb.toString()
    }

    fun registerUser(view: View) {
        val name = nameEditText.text.toString()
        val lastName = lastNameEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (name.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = generateUserId()
                        val user = mAuth.currentUser
                        val userDocRef = db.collection("userProfiles").document(user?.uid ?: "")

                        val userData = hashMapOf(
                            "firstName" to name,
                            "lastName" to lastName,
                            "email" to email,
                            "userId" to userId
                        )

                        userDocRef.set(userData).addOnSuccessListener {
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }.addOnFailureListener { e ->
                            Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
        }
    }

    fun openLoginScreen(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}
