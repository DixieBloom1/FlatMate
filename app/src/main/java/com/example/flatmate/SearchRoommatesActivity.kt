package com.example.flatmate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.flatmate.adapters.AdsAdapter
import com.example.flatmate.models.Ad
import com.google.firebase.firestore.FirebaseFirestore

class SearchRoommatesActivity : AppCompatActivity(), AdsAdapter.OnAdClickListener {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var adsRecyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adsList: MutableList<Ad>  // Holds the full list from Firestore
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use the updated layout which includes a SwipeRefreshLayout and a SearchView
        setContentView(R.layout.activity_search_roommates)

        db = FirebaseFirestore.getInstance()

        // Initialize views from the layout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        adsRecyclerView = findViewById(R.id.adsRecyclerView)
        searchView = findViewById(R.id.searchView)
        val addNewAdButton: Button = findViewById(R.id.addNewAdButton)

        // Initialize list for ads; this will hold the full data set
        adsList = mutableListOf()

        // Set the RecyclerView's layout manager
        adsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Set the pull-to-refresh listener to refresh ads from the DB
        swipeRefreshLayout.setOnRefreshListener {
            refreshAds()
        }

        // Set listener for "Add New Ad" button
        addNewAdButton.setOnClickListener {
            val intent = Intent(this, NewAdActivity::class.java)
            startActivity(intent)
        }

        // Set up the search view listener to filter ads locally
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { filterAds(it) }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterAds(it) }
                return true
            }
        })

        // Do an initial refresh of the ads
        refreshAds()
    }

    private fun refreshAds() {
        db.collection("ads").get()
            .addOnSuccessListener { documents ->
                adsList.clear()
                for (document in documents) {
                    val ad = document.toObject(Ad::class.java)
                    adsList.add(ad)
                }
                // Check if a search query is active, and apply it; otherwise, show the full list
                val currentQuery = searchView.query?.toString() ?: ""
                if (currentQuery.isEmpty()) {
                    val adapter = AdsAdapter(adsList, this)
                    adsRecyclerView.adapter = adapter
                } else {
                    filterAds(currentQuery)
                }
                swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting ads: ${exception.message}", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
            }
    }

    private fun filterAds(query: String) {
        val filteredList = adsList.filter { ad ->
            ad.title.contains(query, ignoreCase = true)
        }
        val adapter = AdsAdapter(filteredList, this)
        adsRecyclerView.adapter = adapter
    }

    override fun onAdClick(ad: Ad) {
        val intent = Intent(this, AdDetailActivity::class.java)
        intent.putExtra("adDetails", ad)
        startActivity(intent)
    }
}
