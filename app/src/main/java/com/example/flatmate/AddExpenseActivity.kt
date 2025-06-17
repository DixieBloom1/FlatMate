package com.example.flatmate

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// Data class representing an expense.
data class Expense(
    var id: String = "",
    var description: String = "",
    var totalAmount: Double = 0.0,
    var splits: Map<String, Double> = mutableMapOf(),
    var createdBy: String = ""
)

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var descriptionEditText: EditText
    private lateinit var totalAmountEditText: EditText
    private lateinit var splitMethodRadioGroup: RadioGroup
    private lateinit var manualSplitsLayout: LinearLayout
    private lateinit var remainingDebtTextView: TextView
    private lateinit var addExpenseButton: Button

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var customUserId: String = ""  // current user's custom ID (from "userId" field)
    private var roommatesList = mutableListOf<Map<String, String>>() // Each map: "userId" and "firstName"
    private val manualSplits = mutableMapOf<String, Double>()         // key: participant custom ID, value: allocated amount

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        descriptionEditText = findViewById(R.id.expenseDescriptionEditText)
        totalAmountEditText = findViewById(R.id.expenseTotalAmountEditText)
        splitMethodRadioGroup = findViewById(R.id.splitMethodRadioGroup)
        manualSplitsLayout = findViewById(R.id.manualSplitsLayout)
        remainingDebtTextView = findViewById(R.id.remainingDebtTextView)
        addExpenseButton = findViewById(R.id.addExpenseButton)

        // Listen to changes in total amount.
        totalAmountEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // In equal mode, recalc equal splits.
                if (splitMethodRadioGroup.checkedRadioButtonId == R.id.equalSplitRadioButton) {
                    updateEqualSplit()
                }
                updateRemainingDebt()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Listen to changes in split method.
        splitMethodRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.equalSplitRadioButton) {
                manualSplitsLayout.visibility = View.GONE
                updateEqualSplit()
            } else { // Manual Split mode.
                // Clear any previously set allocations (from equal-split mode) so that manual fields start fresh.
                manualSplits.clear()
                manualSplitsLayout.visibility = View.VISIBLE
                populateManualSplitsFields()
                updateRemainingDebt()
            }
        }

        addExpenseButton.setOnClickListener {
            addExpense()
        }

        // Load current user's custom ID and roommates.
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            db.collection("userProfiles").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        customUserId = document.getString("userId") ?: ""
                        // Get roommate custom IDs stored in "roommates"
                        val roommatesIds = document.get("roommates") as? List<String> ?: emptyList()
                        fetchRoommates(roommatesIds)
                    }
                }
        }
    }

    // Fetch each roommate's info using their custom user ID.
    private fun fetchRoommates(roommateIds: List<String>) {
        roommatesList.clear()
        for (roommateId in roommateIds) {
            db.collection("userProfiles")
                .whereEqualTo("userId", roommateId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val doc = querySnapshot.documents[0]
                        val firstName = doc.getString("firstName") ?: "Unknown"
                        val data = mapOf("userId" to roommateId, "firstName" to firstName)
                        roommatesList.add(data)
                        // If manual mode is active, refresh the manual splits UI.
                        if (splitMethodRadioGroup.checkedRadioButtonId == R.id.manualSplitRadioButton) {
                            populateManualSplitsFields()
                        }
                    }
                }
        }
    }

    // Clears and repopulates manual splits fields for "You" and for each roommate.
    private fun populateManualSplitsFields() {
        manualSplitsLayout.removeAllViews()
        addSelfManualSplitField()
        for (roommate in roommatesList) {
            val roommateId = roommate["userId"] ?: ""
            val firstName = roommate["firstName"] ?: "Unknown"
            addManualSplitField(roommateId, firstName)
        }
    }

    // Creates a manual split input field for the current user.
    private fun addSelfManualSplitField() {
        if (customUserId.isEmpty()) return
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 8, 0, 8)
        }
        val label = TextView(this).apply {
            text = "You:"
            textSize = 16f
            setPadding(0, 0, 8, 0)
        }
        val amountEditText = EditText(this).apply {
            hint = "0.00"
            setText("0.00")
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        amountEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val value = s.toString().toDoubleOrNull() ?: 0.0
                manualSplits[customUserId] = value
                updateRemainingDebt()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })
        container.addView(label)
        container.addView(amountEditText)
        manualSplitsLayout.addView(container, 0)
    }

    // Creates a manual split input field for a roommate.
    private fun addManualSplitField(roommateId: String, firstName: String) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 8, 0, 8)
        }
        val label = TextView(this).apply {
            text = "$firstName:"
            textSize = 16f
            setPadding(0, 0, 8, 0)
        }
        val amountEditText = EditText(this).apply {
            hint = "0.00"
            setText("0.00")
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        amountEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val value = s.toString().toDoubleOrNull() ?: 0.0
                manualSplits[roommateId] = value
                updateRemainingDebt()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })
        container.addView(label)
        container.addView(amountEditText)
        manualSplitsLayout.addView(container)
    }

    // For equal split mode: evenly divide the total among all participants.
    private fun updateEqualSplit() {
        val totalAmount = totalAmountEditText.text.toString().toDoubleOrNull() ?: 0.0
        val totalParticipants = roommatesList.size + 1  // current user + roommates
        val equalShare = if (totalParticipants > 0) totalAmount / totalParticipants else 0.0
        manualSplits.clear()
        if (customUserId.isNotEmpty()) {
            manualSplits[customUserId] = equalShare
        }
        for (roommate in roommatesList) {
            val id = roommate["userId"] ?: ""
            manualSplits[id] = equalShare
        }
        remainingDebtTextView.text = "Remaining debt: 0.00"
    }

    // Calculates and updates the remaining debt display.
    private fun updateRemainingDebt() {
        val totalAmount = totalAmountEditText.text.toString().toDoubleOrNull() ?: 0.0
        val allocated = manualSplits.values.sum()
        val remaining = totalAmount - allocated
        remainingDebtTextView.text = "Remaining debt: ${"%.2f".format(remaining)}"
    }

    // Validates input and adds the expense document. Then updates every involved user's document.
    private fun addExpense() {
        val description = descriptionEditText.text.toString().trim()
        val totalAmount = totalAmountEditText.text.toString().toDoubleOrNull() ?: 0.0

        if (description.isEmpty() || totalAmount <= 0) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // In manual mode, ensure allocations sum exactly to the total.
        if (splitMethodRadioGroup.checkedRadioButtonId == R.id.manualSplitRadioButton) {
            val allocated = manualSplits.values.sum()
            if (allocated != totalAmount) {
                Toast.makeText(
                    this,
                    "Please allocate the full amount. Remaining: ${"%.2f".format(totalAmount - allocated)}",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }
        // For equal mode manualSplits already holds the equal amounts.
        val splits = HashMap(manualSplits)

        val involvedUsers = mutableSetOf<String>()
        if (customUserId.isNotEmpty()) {
            involvedUsers.add(customUserId)
        }
        involvedUsers.addAll(splits.keys)

// Build expense data including the new field.
        val expenseData = hashMapOf(
            "description" to description,
            "totalAmount" to totalAmount,
            "splits" to splits,
            "createdBy" to customUserId,
            "involvedUsers" to involvedUsers.toList()
        )

        db.collection("expenses")
            .add(expenseData)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show()
                val expenseId = documentReference.id

                // Build the set of involved users: current user + all keys in splits.
                val involvedUsers = mutableSetOf<String>()
                if (customUserId.isNotEmpty()) involvedUsers.add(customUserId)
                involvedUsers.addAll(splits.keys)

                // For each involved user, add the expense ID to their "expenses" array.
                for (userId in involvedUsers) {
                    db.collection("userProfiles")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val docRef = querySnapshot.documents[0].reference
                                docRef.update("expenses", FieldValue.arrayUnion(expenseId))
                            }
                        }
                }
                finish() // Close the activity.
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding expense: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
