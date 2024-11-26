package com.example.mushroomhunters.ui.trips

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mushroomhunters.R
import com.example.mushroomhunters.database.DatabaseHelper
import com.example.mushroomhunters.helper.Utility
import com.example.mushroomhunters.helper.ViewModels

class TripFragment : Fragment(), TripAdapter.OnTripClickListener {

    private lateinit var tripAdapter: TripAdapter
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var tripRecyclerView: RecyclerView
    private lateinit var searchTripEditText: EditText
    private lateinit var addNewTripButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.trip_fragment, container, false)

        tripRecyclerView = view.findViewById(R.id.tripRecyclerView)
        tripRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        databaseHelper = DatabaseHelper(requireContext())
        val tripList = databaseHelper.fetchAllTrips() ?: emptyList()

        tripAdapter = TripAdapter(tripList, this)
        tripRecyclerView.adapter = tripAdapter

        searchTripEditText = view.findViewById(R.id.tripSearchEditText)

        searchTripEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tripAdapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        addNewTripButton = view.findViewById(R.id.addTripButton)
        val navController = findNavController()

        addNewTripButton.setOnClickListener {
            navController.navigate(R.id.nav_trip_form)
        }
        return view
    }

    override fun onEdit(trip: ViewModels.Trips) {
        val navController = findNavController()
        val bundle = Bundle().apply {
            putInt("Id", trip.id)
        }
        navController.navigate(R.id.nav_trip_form, bundle)
    }

    override fun onDelete(trip: ViewModels.Trips) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Trip")
            .setMessage("Are you sure you want to delete this trip?")
            .setPositiveButton("Yes") { dialog, _ ->
                databaseHelper.deleteTrip(trip.id)
                dialog.dismiss()

                Toast.makeText(requireContext(), "Trip deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onViewDetails(trip: ViewModels.Trips) {
        val navController = findNavController()
        val bundle = Bundle().apply {
            putInt("Id", trip.id)
        }
        navController.navigate(R.id.nav_detail_trip, bundle)
    }
}
