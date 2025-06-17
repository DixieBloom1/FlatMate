package com.example.flatmate.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flatmate.R
import com.example.flatmate.models.Ad

class AdsAdapter(private val adsList: List<Ad>, private val onAdClickListener: OnAdClickListener) : RecyclerView.Adapter<AdsAdapter.AdViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_ad, parent, false)
        return AdViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdViewHolder, position: Int) {
        val ad = adsList[position]
        holder.title.text = ad.title
        holder.name.text = "Name: ${ad.name} ${ad.surname}"
        holder.dateOfBirth.text = "Date of Birth: ${ad.birthYear}"


        holder.itemView.setOnClickListener {
            onAdClickListener.onAdClick(ad)
        }
    }

    override fun getItemCount(): Int = adsList.size

    inner class AdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.adTitle)
        val name: TextView = itemView.findViewById(R.id.adName)
        val dateOfBirth: TextView = itemView.findViewById(R.id.adDateOfBirth)
    }

    interface OnAdClickListener {
        fun onAdClick(ad: Ad)
    }
}
