package com.example.mushroomhunters.ui.mushrooms

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mushroomhunters.R
import com.example.mushroomhunters.helper.Utility
import com.example.mushroomhunters.helper.ViewModels

class MushroomListAdapter(
    private var mushroomList: List<ViewModels.Mushrooms>,
    private val listener: OnMushroomClickListener
) : RecyclerView.Adapter<MushroomListAdapter.MushroomViewHolder>() {

    private var fullList: List<ViewModels.Mushrooms> = mushroomList
    private lateinit var utility: Utility

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MushroomViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.mushroom_item, parent, false)
        return MushroomViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MushroomViewHolder, position: Int) {
        val currentMushroom = mushroomList[position]
        utility = Utility()

        holder.mushroomName.text = currentMushroom.type
        holder.mushroomLocation.text = currentMushroom.location
        holder.mushroomQuantity.text = "Quantity: ${currentMushroom.quantity}"

        if (!currentMushroom.image.isNullOrEmpty()) {
            val bitmap = utility.bitmapFromFile(currentMushroom.image)
            holder.mushroomImage.setImageBitmap(bitmap)
        }

        holder.editButton.setOnClickListener {
            listener.onEdit(currentMushroom)
        }

        holder.deleteButton.setOnClickListener {
            listener.onDelete(currentMushroom)
        }

        holder.mushroomImage.setOnClickListener {
            listener.onViewDetails(currentMushroom)
        }
    }

    override fun getItemCount(): Int {
        return mushroomList.size
    }

    fun updateMushroomList(newMushroomList: List<ViewModels.Mushrooms>) {
        mushroomList = newMushroomList
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val filteredList = if (query.isEmpty()) {
            fullList
        } else {
            fullList.filter { mushroom ->
                mushroom.type.contains(query, ignoreCase = true)
            }
        }
        updateMushroomList(filteredList)
    }

    class MushroomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mushroomImage: ImageView = itemView.findViewById(R.id.mushroomImage)
        val mushroomName: TextView = itemView.findViewById(R.id.mushroomName)
        val mushroomLocation: TextView = itemView.findViewById(R.id.mushroomLocation)
        val mushroomQuantity: TextView = itemView.findViewById(R.id.mushroomQuantity)

        val editButton: ImageView = itemView.findViewById(R.id.editMushroomButton)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteMushroomButton)
    }

    interface OnMushroomClickListener {
        fun onEdit(mushroom: ViewModels.Mushrooms)
        fun onDelete(mushroom: ViewModels.Mushrooms)
        fun onViewDetails(mushroom: ViewModels.Mushrooms)
    }
}
