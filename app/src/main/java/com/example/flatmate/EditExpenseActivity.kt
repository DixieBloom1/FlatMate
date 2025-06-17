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

class EditExpenseActivity : AppCompatActivity() {

    private lateinit var descriptionEditText: EditText
    private lateinit var totalAmountEditText: EditText
    private lateinit var splitMethodRadioGroup: RadioGroup
    private lateinit var manualSplitsLayout: LinearLayout
    private lateinit var remainingDebtTextView: TextView
    private lateinit var updateExpenseButton: Button

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Expense ID passed via intent extra.
    private var expenseId: String = ""
    // Current user's custom ID (from "userId")
    private var customUserId: String = ""
    // Stores roommate details (each a map with keys "userId" and "firstName")
    private var roommatesList = mutableListOf<Map<String, String>>()
    // Holds the allocated amounts keyed by custom user IDs.
    private val manualSplits = mutableMapOf<String, Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_expense)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Link UI elements.
        descriptionEditText = findViewById(R.id.expenseDescriptionEditText)
        totalAmountEditText = findViewById(R.id.expenseTotalAmountEditText)
        splitMethodRadioGroup = findViewById(R.id.splitMethodRadioGroup)
        manualSplitsLayout = findViewById(R.id.manualSplitsLayout)
        remainingDebtTextView = findViewById(R.id.remainingDebtTextView)
        updateExpenseButton = findViewById(R.id.updateExpenseButton)

        // Watcher for total amount changes.
        totalAmountEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (splitMethodRadioGroup.checkedRadioButtonId == R.id.equalSplitRadioButton) {
                    updateEqualSplit()
                }
                updateRemainingDebt()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int){}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int){}
        })

        // When switching split mode.
        splitMethodRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.equalSplitRadioButton) {
                manualSplitsLayout.visibility = View.GONE
                updateEqualSplit()
            } else {
                manualSplits.clear() // clear equal split values
                manualSplitsLayout.visibility = View.VISIBLE
                populateManualSplitsFields()
                updateRemainingDebt()
            }
        }

        updateExpenseButton.setOnClickListener {
            updateExpense()
        }

        // Retrieve the expense ID from the Intent.
        expenseId = intent.getStringExtra("expenseId") ?: ""
        if (expenseId.isEmpty()) {
            Toast.makeText(this, "Expense not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        loadExpenseData()
    }

    // Load current user info, then load the expense data.
    private fun loadExpenseData() {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            db.collection("userProfiles").document(currentUser.uid).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    customUserId = doc.getString("userId") ?: ""
                    val roommatesIds = doc.get("roommates") as? List<String> ?: emptyList()
                    fetchRoommates(roommatesIds) {
                        db.collection("expenses").document(expenseId).get().addOnSuccessListener { expenseDoc ->
                            if (expenseDoc.exists()) {
                                val description = expenseDoc.getString("description") ?: ""
                                val totalAmount = expenseDoc.getDouble("totalAmount") ?: 0.0
                                val splits = expenseDoc.get("splits") as? Map<String, Double> ?: emptyMap<String, Double>()

                                descriptionEditText.setText(description)
                                totalAmountEditText.setText(totalAmount.toString())
                                // For editing, force manual mode.
                                splitMethodRadioGroup.check(R.id.manualSplitRadioButton)
                                manualSplitsLayout.visibility = View.VISIBLE
                                manualSplits.clear()
                                manualSplits.putAll(splits)
                                populateManualSplitsFieldsWithValues(splits)
                                updateRemainingDebt()
                            }
                        }
                    }
                }
            }
        }
    }

    // Fetch roommate info, then call onComplete.
    private fun fetchRoommates(roommateIds: List<String>, onComplete: () -> Unit) {
        roommatesList.clear()
        if (roommateIds.isEmpty()) {
            onComplete()
            return
        }
        var count = 0
        for (roommateId in roommateIds) {
            db.collection("userProfiles")
                .whereEqualTo("userId", roommateId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    count++
                    if (!querySnapshot.isEmpty) {
                        val doc = querySnapshot.documents[0]
                        val firstName = doc.getString("firstName") ?: "Unknown"
                        roommatesList.add(mapOf("userId" to roommateId, "firstName" to firstName))
                    }
                    if (count == roommateIds.size)
                        onComplete()
                }
                .addOnFailureListener {
                    count++
                    if (count == roommateIds.size)
                        onComplete()
                }
        }
    }

    // Populate manual split input fields with default (0.0) values.
    private fun populateManualSplitsFields() {
        manualSplitsLayout.removeAllViews()
        addSelfManualSplitField(0.0)
        for (roommate in roommatesList) {
            val rId = roommate["userId"] ?: ""
            val name = roommate["firstName"] ?: "Unknown"
            addManualSplitField(rId, name, 0.0)
        }
    }

    // Populate manual fields using the existing expense splits.
    private fun populateManualSplitsFieldsWithValues(existingSplits: Map<String, Double>) {
        manualSplitsLayout.removeAllViews()
        addSelfManualSplitField(existingSplits[customUserId] ?: 0.0)
        for (roommate in roommatesList) {
            val rId = roommate["userId"] ?: ""
            val name = roommate["firstName"] ?: "Unknown"
            addManualSplitField(rId, name, existingSplits[rId] ?: 0.0)
        }
    }

    // Adds a field for the current user with an initial value.
    private fun addSelfManualSplitField(initialValue: Double) {
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
            setText(String.format("%.2f", initialValue))
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

    // Adds a field for a roommate with an initial value.
    private fun addManualSplitField(roommateId: String, firstName: String, initialValue: Double) {
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
            setText(String.format("%.2f", initialValue))
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

    // In equal mode, divide the total among all participants.
    private fun updateEqualSplit() {
        val totalAmount = totalAmountEditText.text.toString().toDoubleOrNull() ?: 0.0
        val totalParticipants = roommatesList.size + 1
        val equalShare = if (totalParticipants > 0) totalAmount / totalParticipants else 0.0
        manualSplits.clear()
        if (customUserId.isNotEmpty()) manualSplits[customUserId] = equalShare
        for (roommate in roommatesList) {
            val id = roommate["userId"] ?: ""
            manualSplits[id] = equalShare
        }
        remainingDebtTextView.text = "Remaining debt: 0.00"
    }

    // Updates the remaining debt display.
    private fun updateRemainingDebt() {
        val totalAmount = totalAmountEditText.text.toString().toDoubleOrNull() ?: 0.0
        val allocated = manualSplits.values.sum()
        val remaining = totalAmount - allocated
        remainingDebtTextView.text = "Remaining debt: ${"%.2f".format(remaining)}"
    }

    // Update the expense document in Firestore.
    private fun updateExpense() {
        val description = descriptionEditText.text.toString().trim()
        val totalAmount = totalAmountEditText.text.toString().toDoubleOrNull() ?: 0.0

        if (description.isEmpty() || totalAmount <= 0) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (splitMethodRadioGroup.checkedRadioButtonId == R.id.manualSplitRadioButton) {
            val allocated = manualSplits.values.sum()
            if (allocated != totalAmount) {
                Toast.makeText(this,
                    "Please allocate the full amount. Remaining: ${"%.2f".format(totalAmount - allocated)}",
                    Toast.LENGTH_SHORT).show()
                return
            }
        }

        val splits = HashMap(manualSplits)
        val involvedUsers = mutableSetOf<String>()
        if (customUserId.isNotEmpty()) involvedUsers.add(customUserId)
        involvedUsers.addAll(splits.keys)

        val updatedData = hashMapOf(
            "description" to description,
            "totalAmount" to totalAmount,
            "splits" to splits,
            "involvedUsers" to involvedUsers.toList()
        )

        db.collection("expenses").document(expenseId)
            .update(updatedData)
            .addOnSuccessListener {
                Toast.makeText(this, "Expense updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating expense: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
