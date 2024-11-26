package com.example.mushroomhunters.ui.mushrooms

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

class MushroomFragment : Fragment(), MushroomListAdapter.OnMushroomClickListener {

    private lateinit var mushroomListAdapter: MushroomListAdapter
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var mushroomRecyclerView: RecyclerView
    private lateinit var mushroomSearchEditText: EditText
    private lateinit var addMushroomButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.mushroom_fragment, container, false)

        mushroomRecyclerView = view.findViewById(R.id.mushroomRecyclerView)
        mushroomRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        databaseHelper = DatabaseHelper(requireContext())
        val mushrooms = databaseHelper.fetchAllMushrooms() ?: emptyList()

        mushroomListAdapter = MushroomListAdapter(mushrooms, this)
        mushroomRecyclerView.adapter = mushroomListAdapter

        mushroomSearchEditText = view.findViewById(R.id.mushroomSearchEditText)
        mushroomSearchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mushroomListAdapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        addMushroomButton = view.findViewById(R.id.addMushroomButton)
        val navController = findNavController()

        addMushroomButton.setOnClickListener {
            navController.navigate(R.id.nav_mushroom_form)
        }

        return view
    }

    override fun onEdit(mushroom: ViewModels.Mushrooms) {
        val navController = findNavController()
        val bundle = Bundle().apply {
            putInt("mushroomId", mushroom.id)
        }
        navController.navigate(R.id.nav_mushroom_form, bundle)
    }

    override fun onDelete(mushroom: ViewModels.Mushrooms) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Mushroom")
            .setMessage("Are you sure you want to delete this mushroom?")
            .setPositiveButton("Yes") { dialog, _ ->
                databaseHelper.removeMushroom(mushroom.id)
                dialog.dismiss()
                Toast.makeText(requireContext(), "Mushroom deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    override fun onViewDetails(mushroom: ViewModels.Mushrooms) {
        val navController = findNavController()
        val bundle = Bundle().apply {
            putInt("mushroomId", mushroom.id)
        }
        navController.navigate(R.id.nav_detail_mushroom, bundle)
    }
}
