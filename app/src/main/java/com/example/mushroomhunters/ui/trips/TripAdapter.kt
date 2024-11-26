package com.example.mushroomhunters.ui.trips

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mushroomhunters.R
import com.example.mushroomhunters.database.DatabaseHelper
import com.example.mushroomhunters.helper.Utility
import com.example.mushroomhunters.helper.ViewModels


class TripAdapter(
    private var list: List<ViewModels.Trips>,
    private val listener: OnTripClickListener 

) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    private var fullList: List<ViewModels.Trips> = list
    private lateinit var utility: Utility

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.trip_item, parent, false)
        return TripViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val current = list[position]
        utility= Utility()
        holder.tripName.text = current.name
        holder.tripDuration.text = current.duration
        holder.tripLocation.text = current.location
        holder.tripDate.text = current.date

        if (!current.image.isNullOrEmpty()) {
            val bitmap = utility.bitmapFromFile(current.image)
            holder.tripImage.setImageBitmap(bitmap)
        }
        holder.editTripButton.setOnClickListener {
            listener.onEdit(current)
        }

        holder.deleteTripButton.setOnClickListener {
            listener.onDelete(current)
        }

        holder.tripImage.setOnClickListener {
            listener.onViewDetails(current) // Show details when image is clicked
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateList(newTripList: List<ViewModels.Trips>) {
        list = newTripList
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val filteredList = if (query.isEmpty()) {
            fullList
        } else {
            fullList.filter { trip ->
                trip.name.contains(query, ignoreCase = true) ||
                        trip.location.contains(query, ignoreCase = true)
            }
        }
        updateList(filteredList)
    }
    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tripImage: ImageView = itemView.findViewById(R.id.tripImage)
        val tripName: TextView = itemView.findViewById(R.id.tripName)
        val tripLocation: TextView = itemView.findViewById(R.id.tripLocation)

        val tripDate: TextView = itemView.findViewById(R.id.tripDate)
        val tripDuration: TextView = itemView.findViewById(R.id.tripDuration)
        val editTripButton: ImageView = itemView.findViewById(R.id.editTripButton)
        val deleteTripButton: ImageView = itemView.findViewById(R.id.deleteTripButton)
    }


    interface OnTripClickListener {
        fun onEdit(Trip: ViewModels.Trips)
        fun onDelete(Trip: ViewModels.Trips)
        fun onViewDetails(Trip: ViewModels.Trips)
    }
}
