package com.example.flatmate

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var newPasswordEditText: EditText
    private lateinit var changePasswordButton: Button

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        mAuth = FirebaseAuth.getInstance()

        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        changePasswordButton = findViewById(R.id.changePasswordButton)

        // Funkcija za promjenu lozinke
        changePasswordButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString()

            if (newPassword.isNotEmpty()) {
                val user = mAuth.currentUser
                user?.updatePassword(newPassword)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter a new password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
