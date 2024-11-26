package com.example.mushroomhunters.ui.recenttrips

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mushroomhunters.R
import com.example.mushroomhunters.helper.Utility
import com.example.mushroomhunters.helper.ViewModels

class RecentTripsAdapter(private var tripsList: List<ViewModels.Trips>) : RecyclerView.Adapter<RecentTripsAdapter.RecentTripViewHolder>() {

    private val allTrips: List<ViewModels.Trips> = tripsList
    private val utility: Utility = Utility()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentTripViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recent_trip_item, parent, false)
        return RecentTripViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecentTripViewHolder, position: Int) {
        val currentTrip = tripsList[position]
        holder.tripNameTextView.text = currentTrip.name
        holder.tripDateTimeTextView.text = "${currentTrip.date} ${currentTrip.time}"
        holder.tripLocationTextView.text = currentTrip.location

        if (!currentTrip.image.isNullOrEmpty()) {
            val bitmap = utility.bitmapFromFile(currentTrip.image)
            holder.tripImageView.setImageBitmap(bitmap)
            holder.tripImageView.visibility = View.VISIBLE
        } else {
            holder.tripImageView.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return tripsList.size
    }

    fun updateTripsList(newTripsList: List<ViewModels.Trips>) {
        tripsList = newTripsList
        notifyDataSetChanged()
    }

    fun filterTrips(query: String) {
        val filteredList = if (query.isEmpty()) {
            allTrips
        } else {
            allTrips.filter { trip ->
                trip.name.contains(query, ignoreCase = true) ||
                        trip.location.contains(query, ignoreCase = true)
            }
        }
        updateTripsList(filteredList)
    }

    class RecentTripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tripImageView: ImageView = itemView.findViewById(R.id.tripImageView)
        val tripNameTextView: TextView = itemView.findViewById(R.id.tripNameTextView)
        val tripDateTimeTextView: TextView = itemView.findViewById(R.id.tripDateTextView)
        val tripLocationTextView: TextView = itemView.findViewById(R.id.tripLocationTextView)
    }
}
