package com.example.flatmate.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flatmate.R

class RoommateAdapter(
    private val roommates: MutableList<Map<String, String>>,
    private val onDeleteClick: (String) -> Unit // Pass a lambda function to handle delete
) : RecyclerView.Adapter<RoommateAdapter.RoommateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoommateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_roommate_card, parent, false)
        return RoommateViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoommateViewHolder, position: Int) {
        val roommate = roommates[position]
        holder.nameTextView.text = "${roommate["firstName"]} ${roommate["lastName"]}"
        holder.userIdTextView.text = "User ID: ${roommate["userId"]}"

        // Set the delete button click listener
        holder.deleteButton.setOnClickListener {
            val roommateId = roommate["userId"] ?: return@setOnClickListener
            onDeleteClick(roommateId)
        }
    }

    override fun getItemCount(): Int = roommates.size

    class RoommateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.roommateNameTextView)
        val userIdTextView: TextView = itemView.findViewById(R.id.roommateUserIdTextView)
        val deleteButton: Button = itemView.findViewById(R.id.deleteRoommateButton)
    }
}
