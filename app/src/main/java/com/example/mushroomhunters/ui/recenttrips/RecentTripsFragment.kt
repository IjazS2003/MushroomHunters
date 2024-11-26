package com.example.mushroomhunters.ui.recenttrips

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mushroomhunters.R
import com.example.mushroomhunters.database.DatabaseHelper

class RecentTripsFragment : Fragment() {

    private lateinit var tripsRecyclerView: RecyclerView
    private lateinit var recentTripsAdapter: RecentTripsAdapter
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var tripSearchEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.recent_fragment, container, false)

        tripsRecyclerView = view.findViewById(R.id.tripRecyclerView)
        tripsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        databaseHelper = DatabaseHelper(requireContext())
      //  databaseHelper.Populatewithsample()
        val recentTrips = databaseHelper.fetchRecentTrips() ?: emptyList()

        recentTripsAdapter = RecentTripsAdapter(recentTrips)
        tripsRecyclerView.adapter = recentTripsAdapter

        tripSearchEditText = view.findViewById(R.id.tripSearchEditText)

        tripSearchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                recentTripsAdapter.filterTrips(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        return view
    }
}
