package com.example.flatmate

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flatmate.adapters.RoommateAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RoommateListActivity : AppCompatActivity() {

    private lateinit var roommatesRecyclerView: RecyclerView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var roommatesList: MutableList<Map<String, String>>
    private lateinit var roommateAdapter: RoommateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        roommatesRecyclerView = findViewById(R.id.roommatesRecyclerView)
        roommatesList = mutableListOf()

        roommatesRecyclerView.layoutManager = LinearLayoutManager(this)
        roommateAdapter = RoommateAdapter(roommatesList) { roommateId ->
            deleteRoommate(roommateId)
        }
        roommatesRecyclerView.adapter = roommateAdapter

        fetchRoommates()
    }

    private fun fetchRoommates() {
        val user = mAuth.currentUser ?: return

        val userDocRef = db.collection("userProfiles").document(user.uid)
        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Retrieve roommate IDs stored (as custom "userId" values)
                val roommates = document.get("roommates") as? List<String> ?: emptyList()

                if (roommates.isEmpty()) {
                    Toast.makeText(this, "No roommates found.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Clear the list for fresh data
                roommatesList.clear()

                // For each roommate ID, query by the "userId" field from Firestore
                for (roommateId in roommates) {
                    db.collection("userProfiles")
                        .whereEqualTo("userId", roommateId)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val roommateDoc = querySnapshot.documents[0]
                                val firstName = roommateDoc.getString("firstName") ?: "Unknown"
                                val lastName = roommateDoc.getString("lastName") ?: "Unknown"

                                val roommateData = mapOf(
                                    "firstName" to firstName,
                                    "lastName" to lastName,
                                    "userId" to roommateId
                                )
                                roommatesList.add(roommateData)
                                roommateAdapter.notifyDataSetChanged()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Roommate with ID $roommateId does not exist!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error fetching roommate data.", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            } else {
                Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error retrieving user data.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteRoommate(roommateId: String) {
        val user = mAuth.currentUser ?: return
        val userDocRef = db.collection("userProfiles").document(user.uid)

        // Retrieve current user's roommate list from Firestore
        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val roommates = document.get("roommates") as? ArrayList<String> ?: arrayListOf()

                // Remove the roommate ID if it exists in the list
                if (roommates.contains(roommateId)) {
                    roommates.remove(roommateId)
                    val newRoommateCount = roommates.size  // Calculate new roommate count

                    // Update both the roommates list and the roommateCount field
                    userDocRef.update("roommates", roommates, "roommateCount", newRoommateCount)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Roommate removed!", Toast.LENGTH_SHORT).show()

                            // If this was the last roommate, navigate back to HomeActivity
                            if (newRoommateCount == 0) {
                                startActivity(Intent(this, HomeActivity::class.java))
                                finish() // Optionally finish current activity
                            } else {
                                fetchRoommates() // Otherwise, refresh the roommate list UI
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to remove roommate.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Roommate not found in your list.", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error retrieving user data.", Toast.LENGTH_SHORT).show()
        }
    }
}
