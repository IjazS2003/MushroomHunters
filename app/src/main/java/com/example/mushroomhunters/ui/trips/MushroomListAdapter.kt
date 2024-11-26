package com.example.mushroomhunters.ui.trips


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mushroomhunters.R
import com.example.mushroomhunters.database.DatabaseHelper
import com.example.mushroomhunters.helper.Utility
import com.example.mushroomhunters.helper.ViewModels

class MushroomListAdapter(private val mushroomList: List<ViewModels.Mushrooms>) :
    RecyclerView.Adapter<MushroomListAdapter.MushroomViewHolder>() {

    class MushroomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemSerialNumber: TextView = itemView.findViewById(R.id.itemSerialNumber)

        val itemType: TextView = itemView.findViewById(R.id.itemType)
        val itemQuantity: TextView = itemView.findViewById(R.id.itemQuantity)
        val itemLocation: TextView = itemView.findViewById(R.id.itemLocation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MushroomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.trip_mushroom_item, parent, false)
        return MushroomViewHolder(view)
    }

    override fun onBindViewHolder(holder: MushroomViewHolder, position: Int) {
        val mushroom = mushroomList[position]
        holder.itemSerialNumber.text = "Mushroom ${position+1} :"

        holder.itemType.text = mushroom.type
        holder.itemQuantity.text = "Quantity: ${mushroom.quantity}"
        holder.itemLocation.text = "Location: ${mushroom.location}"
    }

    override fun getItemCount(): Int {
        return mushroomList.size
    }
}
