package com.example.flatmate

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ManageExpensesActivity : AppCompatActivity() {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var addExpenseFab: FloatingActionButton
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth

    private var expensesList = mutableListOf<Expense>()
    private lateinit var expenseListAdapter: ExpenseAdapter
    private var userNamesMap = mutableMapOf<String, String>()
    private var currentUserCustomId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_expenses)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        expensesRecyclerView = findViewById(R.id.expensesRecyclerView)
        addExpenseFab = findViewById(R.id.addExpenseFab)

        expensesRecyclerView.layoutManager = LinearLayoutManager(this)

        addExpenseFab.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        swipeRefreshLayout.setOnRefreshListener {
            fetchExpenses()
            swipeRefreshLayout.isRefreshing = false
        }

        loadCurrentUserInfoAndProfiles()
    }

    private fun loadCurrentUserInfoAndProfiles() {
        val currentUser = mAuth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection("userProfiles").document(currentUser.uid).get().addOnSuccessListener { profile ->
            if (profile.exists()) {
                currentUserCustomId = profile.getString("userId") ?: ""
                db.collection("userProfiles").get().addOnSuccessListener { profilesSnapshot ->
                    userNamesMap.clear()
                    for (doc in profilesSnapshot.documents) {
                        val customId = doc.getString("userId") ?: continue
                        val firstName = doc.getString("firstName") ?: "Unknown"
                        userNamesMap[customId] = firstName
                    }
                    fetchExpenses()
                }
            }
        }
    }

    // Query only expenses where the current user is involved.
    private fun fetchExpenses() {
        db.collection("expenses")
            .whereArrayContains("involvedUsers", currentUserCustomId)
            .get()
            .addOnSuccessListener { expensesSnapshot ->
                expensesList.clear()
                for (document in expensesSnapshot.documents) {
                    val expense = document.toObject(Expense::class.java)
                    if (expense != null) {
                        expense.id = document.id
                        expensesList.add(expense)
                    }
                }
                expenseListAdapter = ExpenseAdapter(
                    expensesList,
                    userNamesMap,
                    currentUserCustomId,
                    onEditExpense = { expense ->
                        // Start EditExpenseActivity with expenseId extra.
                        val intent = Intent(this, EditExpenseActivity::class.java)
                        intent.putExtra("expenseId", expense.id)
                        startActivity(intent)
                    },
                    onDeleteExpense = { expense ->
                        db.collection("expenses").document(expense.id)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show()
                                fetchExpenses()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error deleting expense: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                )
                expensesRecyclerView.adapter = expenseListAdapter
            }
    }
}
