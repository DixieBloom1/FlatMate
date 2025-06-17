package com.example.flatmate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExpenseAdapter(
    private val expenses: List<Expense>,
    private val userNamesMap: Map<String, String>,
    private val currentUserId: String,
    private val onEditExpense: (Expense) -> Unit,
    private val onDeleteExpense: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionTextView: TextView = itemView.findViewById(R.id.expenseDescription)
        val totalAmountTextView: TextView = itemView.findViewById(R.id.expenseTotalAmount)
        val splitsTextView: TextView = itemView.findViewById(R.id.expenseSplits)
        val createdByTextView: TextView = itemView.findViewById(R.id.expenseCreatedBy)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.descriptionTextView.text = expense.description
        holder.totalAmountTextView.text = "Total: €${expense.totalAmount}"

        // Convert split entries to name: amount format.
        val splitsDisplay = expense.splits.entries.joinToString(separator = ", ") { entry ->
            val name = userNamesMap[entry.key] ?: entry.key
            "$name: €${entry.value}"
        }
        holder.splitsTextView.text = "Splits: $splitsDisplay"

        val creatorName = userNamesMap[expense.createdBy] ?: expense.createdBy
        holder.createdByTextView.text = "Created by: $creatorName"

        // Only show Edit and Delete buttons if the current user is the creator.
        if (expense.createdBy == currentUserId) {
            holder.btnEdit.visibility = View.VISIBLE
            holder.btnDelete.visibility = View.VISIBLE

            holder.btnEdit.setOnClickListener { onEditExpense(expense) }
            holder.btnDelete.setOnClickListener { onDeleteExpense(expense) }
        } else {
            holder.btnEdit.visibility = View.GONE
            holder.btnDelete.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = expenses.size
}
