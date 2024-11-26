package com.example.mushroomhunters.ui.mushrooms

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mushroomhunters.R
import com.example.mushroomhunters.database.DatabaseHelper
import com.example.mushroomhunters.helper.LocationHelper
import com.example.mushroomhunters.helper.Utility

class AddMushroomFragment : Fragment() {

    private lateinit var mushroomNameEditText: EditText
    private lateinit var mushroomLocationEditText: EditText
    private lateinit var mushroomQuantityEditText: EditText
    private lateinit var mushroomCommentEditText: EditText
    private lateinit var tripSpinner: Spinner
    private lateinit var mushroomImageView: ImageView
    private lateinit var imageSelectButton: Button
    private lateinit var saveButton: Button
    private lateinit var locationButton: ImageButton

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var utility: Utility
    private lateinit var locationHelper: LocationHelper

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var mushroomId = -1
    private var mushroomBitmap: Bitmap? = null

    private val CAMERA_PERMISSION_CODE = 101
    private val GALLERY_PERMISSION_CODE = 102

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_mushroom_fragment, container, false)

        // Initialize views
        mushroomNameEditText = view.findViewById(R.id.mushroomNameEditText)
        mushroomLocationEditText = view.findViewById(R.id.locationEditText)
        mushroomQuantityEditText = view.findViewById(R.id.mushroomQuantityEditText)
        mushroomCommentEditText = view.findViewById(R.id.commentsEditText)
        tripSpinner = view.findViewById(R.id.tripSelectionSpinner)
        mushroomImageView = view.findViewById(R.id.mushroomImageView)
        imageSelectButton = view.findViewById(R.id.selectImageButton)
        saveButton = view.findViewById(R.id.saveMushroomButton)
        locationButton = view.findViewById(R.id.getCurrentLocationButton)

        // Initialize helpers
        databaseHelper = DatabaseHelper(requireContext())
        utility = Utility()
        locationHelper = LocationHelper(requireContext())

        mushroomId = arguments?.getInt("mushroomId", -1) ?: -1

        // Load trip data into spinner
        val trips = databaseHelper.fetchAllTrips()
        val tripNames = trips.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tripNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tripSpinner.adapter = adapter

        // Load existing mushroom data if editing
        if (mushroomId != -1) {
            val mushroom = databaseHelper.fetchMushroomById(mushroomId)
            mushroom?.let {
                mushroomNameEditText.setText(it.type)
                mushroomLocationEditText.setText(it.location)
                mushroomCommentEditText.setText(it.comments)
                mushroomQuantityEditText.setText(it.quantity.toString())
                val tripIndex = trips.indexOfFirst { trip -> trip.id == it.tripId }
                if (tripIndex != -1) {
                    tripSpinner.setSelection(tripIndex)
                }
                if (it.image?.isNotEmpty() == true) {
                    val image = utility.bitmapFromFile(it.image)
                    mushroomBitmap = image
                    mushroomImageView.setImageBitmap(mushroomBitmap)
                }
            }
        }

        // Set listeners for image selection and save
        imageSelectButton.setOnClickListener { openImagePicker() }
        saveButton.setOnClickListener { saveMushroom() }
        locationButton.setOnClickListener { getLocation() }

        locationHelper.startLocationUpdates()

        return view
    }

    private fun openImagePicker() {
        val options = arrayOf("Camera", "Gallery")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Select Image Source")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
                imagePickerResult.launch(takePictureIntent)
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }

    private fun openGallery() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerResult.launch(intent)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                GALLERY_PERMISSION_CODE
            )
        }
    }

    private val imagePickerResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                result.data?.let { data ->
                    val imageUri: Uri? = data.data
                    if (imageUri != null) {
                        mushroomBitmap = BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(imageUri))
                        mushroomImageView.setImageBitmap(mushroomBitmap)
                    } else {
                        mushroomBitmap = data.extras?.get("data") as Bitmap
                        mushroomImageView.setImageBitmap(mushroomBitmap)
                    }
                }
            }
        }

    private fun saveMushroom() {
        val name = mushroomNameEditText.text.toString()
        val location = mushroomLocationEditText.text.toString()
        val qty = mushroomQuantityEditText.text.toString()
        val description = mushroomCommentEditText.text.toString()

        val tripId = tripSpinner.selectedItemPosition.let { position ->
            if (position != AdapterView.INVALID_POSITION) {
                val selectedTrip = databaseHelper.fetchAllTrips()[position]
                selectedTrip.id
            } else {
                null
            }
        }

        val imageUrl = mushroomBitmap?.let { utility.saveBitmap(requireContext(), it) }

        if (name.isEmpty() || location.isEmpty() || qty.isEmpty() || tripId == null) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (mushroomId == -1) {
            databaseHelper.addMushroom(tripId, name, location, qty.toInt(), description, longitude, latitude, imageUrl)
        } else {
            databaseHelper.editMushroom(mushroomId, tripId, name, location, qty.toInt(), description, longitude, latitude, imageUrl)
        }

        locationHelper.stopLocationUpdates()
        findNavController().popBackStack()
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        } else {
            locationHelper.getLastKnownLocation { location, address, latitude, longitude ->
                if (location != null && address != null && latitude != null && longitude != null) {
                    mushroomLocationEditText.setText(address)
                    this.longitude = longitude
                    this.latitude = latitude
                } else {
                    Toast.makeText(requireContext(), "Unable to fetch location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                }
            }
            GALLERY_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                }
            }
            LOCATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation()
                } else {
                    Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_CODE = 1001
    }
}
