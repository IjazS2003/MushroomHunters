package com.example.mushroomhunters.ui.trips

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mushroomhunters.R
import com.example.mushroomhunters.database.DatabaseHelper
import com.example.mushroomhunters.helper.Utility
import com.example.mushroomhunters.helper.ViewModels

class ViewTripFragment : Fragment() {

    private lateinit var tripImageView: ImageView
    private lateinit var tripNameTextView: TextView
    private lateinit var tripDateTextView: TextView
    private lateinit var tripLocationTextView: TextView
    private lateinit var tripDurationTextView: TextView
    private lateinit var tripDescriptionTextView: TextView
    private lateinit var mushroomsRecyclerView: RecyclerView
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var utility: Utility
    private var tripId: Int = -1

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.detail_trip_fragment, container, false)

        tripImageView = view.findViewById(R.id.tripImageView)
        tripNameTextView = view.findViewById(R.id.tripNameTextView)
        tripDateTextView = view.findViewById(R.id.tripDateTextView)
        tripLocationTextView = view.findViewById(R.id.tripLocationTextView)
        tripDurationTextView = view.findViewById(R.id.tripDurationTextView)
        tripDescriptionTextView = view.findViewById(R.id.tripDescriptionTextView)
        mushroomsRecyclerView = view.findViewById(R.id.mushroomsRecyclerView)

        databaseHelper = DatabaseHelper(requireContext())
        utility = Utility()

        tripId = arguments?.getInt("Id") ?: -1

        if (tripId != -1) {
            getTripDetails(tripId)
        }

        return view
    }

    private fun getTripDetails(tripId: Int) {
        val tripData = databaseHelper.fetchTripById(tripId)
        tripData?.let {
            tripNameTextView.text = "Name: ${it.name}"
            tripDateTextView.text = "Date: ${it.date}"
            tripLocationTextView.text = "Location: ${it.location}"
            tripDurationTextView.text = "Duration: ${it.duration}"
            tripDescriptionTextView.text = "Description: ${it.description}"

            if (!it.image.isNullOrEmpty()) {
                val bitmap = utility.bitmapFromFile(it.image)
                tripImageView.setImageBitmap(bitmap)
            }
            it.mushrooms?.let { mushrooms -> loadMushrooms(mushrooms) }
        }
    }

    private fun loadMushrooms(mushrooms: List<ViewModels.Mushrooms>) {
        val mushroomAdapter = MushroomListAdapter(mushrooms)
        mushroomsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        mushroomsRecyclerView.adapter = mushroomAdapter
    }
}
