package com.example.mushroomhunters.ui.upload

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.mushroomhunters.R
import com.example.mushroomhunters.database.DatabaseHelper
import com.example.mushroomhunters.helper.Utility

class UploadFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var uploadButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var requestPayloadTextView: TextView
    private lateinit var responsePayloadTextView: TextView

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var utility: Utility

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.upload_fragment, container, false)
        databaseHelper = DatabaseHelper(requireContext())
        utility = Utility()

        val tripList = databaseHelper.fetchAllTrips() ?: emptyList()

        progressBar = view.findViewById(R.id.loadingProgressBar)
        uploadButton = view.findViewById(R.id.uploadButton)
        statusTextView = view.findViewById(R.id.statusTextView)
        requestPayloadTextView = view.findViewById(R.id.requestPayloadEditText)
        responsePayloadTextView = view.findViewById(R.id.responsePayloadTextView)

        val jsonPayload = utility.createTripPayload(tripList)
        requestPayloadTextView.text = jsonPayload

        uploadButton.setOnClickListener {
            startUpload()
        }

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun startUpload() {
        val tripList = databaseHelper.fetchAllTrips() ?: emptyList()

        progressBar.visibility = View.VISIBLE
        uploadButton.visibility = View.GONE
        statusTextView.text = "Uploading to Server..."

        val jsonPayload = utility.createTripPayload(tripList)

        utility.sendTripData(jsonPayload) { success, message, headers ->
            progressBar.visibility = View.GONE
            uploadButton.visibility = View.VISIBLE

            if (success) {
                statusTextView.text = "Upload Successful: $message"
                headers?.let {
                    Log.d("Response Headers", it.toString())
                }
                showToast("Upload completed successfully!")
            } else {
                statusTextView.text = "Upload Failed: $message"
                showToast("Upload failed. Please try again.")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}
