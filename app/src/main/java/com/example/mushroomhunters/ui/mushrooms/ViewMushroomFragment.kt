package com.example.mushroomhunters.ui.mushrooms

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mushroomhunters.R
import com.example.mushroomhunters.database.DatabaseHelper
import com.example.mushroomhunters.helper.Utility

class ViewMushroomFragment : Fragment() {

    private lateinit var mushroomImageView: ImageView
    private lateinit var mushroomNameTextView: TextView
    private lateinit var quantityTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var tripNameTextView: TextView
    private lateinit var descriptionTextView: TextView

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var utility: Utility
    private var mushroomId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.detail_mushroom_fragment, container, false)

        mushroomImageView = view.findViewById(R.id.mushroomImageView)
        mushroomNameTextView = view.findViewById(R.id.mushroomNameTextView)
        quantityTextView = view.findViewById(R.id.quantityTextView)
        locationTextView = view.findViewById(R.id.locationTextView)
        tripNameTextView = view.findViewById(R.id.tripNameTextView)
        descriptionTextView = view.findViewById(R.id.descriptionTextView)

        databaseHelper = DatabaseHelper(requireContext())
        utility = Utility()

        mushroomId = arguments?.getInt("mushroomId") ?: -1

        if (mushroomId != -1) {
            loadMushroomDetails(mushroomId)
        }

        return view
    }

    @SuppressLint("SetTextI18n")
    private fun loadMushroomDetails(mushroomId: Int) {
        val mushroomData = databaseHelper.fetchMushroomById(mushroomId)
        mushroomData?.let {
            mushroomNameTextView.text = "Name: ${it.type}"
            quantityTextView.text = "Quantity: ${it.quantity}"
            locationTextView.text = "Location: ${it.location}"
            descriptionTextView.text = "Description: ${it.comments}"
            tripNameTextView.text = "Trip: ${it.trip?.name}"

            if (!it.image.isNullOrEmpty()) {
                val bitmap = utility.bitmapFromFile(it.image)
                mushroomImageView.setImageBitmap(bitmap)
            }
        }
    }
}
