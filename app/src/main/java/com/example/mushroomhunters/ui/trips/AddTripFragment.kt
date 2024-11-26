package com.example.mushroomhunters.ui.trips

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mushroomhunters.R
import java.util.Calendar
import com.example.mushroomhunters.database.DatabaseHelper
import com.example.mushroomhunters.helper.LocationHelper
import com.example.mushroomhunters.helper.Utility

class AddTripFragment : Fragment() {

    private lateinit var tripNameEditText: EditText
    private lateinit var locationEditText: EditText
    private lateinit var dateEditText: EditText
    private lateinit var timeEditText: EditText
    private lateinit var durationEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var commentsEditText: EditText

    private lateinit var tripImageView: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var saveTripButton: Button
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var utility: Utility
    private lateinit var getCurrentLocationButton: ImageButton
    private lateinit var locationHelper: LocationHelper
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var tripId: Int = -1
    private var selectedImageBitmap: Bitmap? = null
    private val CAMERA_REQUEST_CODE = 101
    private val GALLERY_REQUEST_CODE = 102

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_trip_fragment, container, false)

        tripNameEditText = view.findViewById(R.id.tripNameEditText)
        locationEditText = view.findViewById(R.id.locationEditText)
        dateEditText = view.findViewById(R.id.dateEditText)
        timeEditText = view.findViewById(R.id.timeEditText)
        durationEditText = view.findViewById(R.id.durationEditText)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)
        commentsEditText = view.findViewById(R.id.commentsEditText)


        tripImageView = view.findViewById(R.id.tripImageView)
        selectImageButton = view.findViewById(R.id.selectImageButton)
        saveTripButton = view.findViewById(R.id.saveTripButton)
        getCurrentLocationButton = view.findViewById(R.id.getCurrentLocationButton)

        databaseHelper = DatabaseHelper(requireContext())
        utility = Utility()

        tripId = arguments?.getInt("Id", -1) ?: -1

        if (tripId != -1) {
            val tripData = databaseHelper.fetchTripById(tripId)
            tripData?.let {
                tripNameEditText.setText(it.name)
                locationEditText.setText(it.location)
                dateEditText.setText(it.date)
                timeEditText.setText(it.time)
                durationEditText.setText(it.duration)
                descriptionEditText.setText(it.description)
                if (it.image?.isNotEmpty() == true) {
                    val image = utility.bitmapFromFile(it.image)
                    selectedImageBitmap = image
                    tripImageView.setImageBitmap(selectedImageBitmap)
                }
            }
        }

        selectImageButton.setOnClickListener { showImagePickerDialog() }
        dateEditText.setOnClickListener { showDatePickerDialog() }
        timeEditText.setOnClickListener { showTimePickerDialog() }
        saveTripButton.setOnClickListener { saveTrip() }

        locationHelper = LocationHelper(requireContext())
        locationHelper.startLocationUpdates()

        getCurrentLocationButton.setOnClickListener { requestLocation() }

        return view
    }

    private fun showImagePickerDialog() {
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
                imagePickerLauncher.launch(takePictureIntent)
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                dateEditText.setText(formattedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                timeEditText.setText(formattedTime)
            },
            hour, minute, true
        )
        timePickerDialog.show()
    }

    private fun openGallery() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                GALLERY_REQUEST_CODE
            )
        }
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                result.data?.let { data ->
                    val imageUri: Uri? = data.data
                    if (imageUri != null) {
                        selectedImageBitmap = BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(imageUri))
                        tripImageView.setImageBitmap(selectedImageBitmap)
                    } else {
                        selectedImageBitmap = data.extras?.get("data") as Bitmap
                        tripImageView.setImageBitmap(selectedImageBitmap)
                    }
                }
            }
        }

    private fun saveTrip() {
        val name = tripNameEditText.text.toString()
        val location = locationEditText.text.toString()
        val date = dateEditText.text.toString()
        val time = timeEditText.text.toString()
        val duration = durationEditText.text.toString()
        val description = descriptionEditText.text.toString()

        val imageUrl = selectedImageBitmap?.let { utility.saveBitmap(requireContext(), it) }

        if (name.isEmpty() || location.isEmpty() || date.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (tripId == -1) {
            databaseHelper.addTrip(name, date, time, location, duration, description, longitude, latitude, imageUrl)
        } else {
            databaseHelper.updateTrip(tripId, name, date, time, location, duration, description, longitude, latitude, imageUrl)
        }

        locationHelper.stopLocationUpdates()
        findNavController().popBackStack()
    }

    private fun requestLocation() {
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
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            locationHelper.getLastKnownLocation { location, address, latitude, longitude ->
                if (location != null && address != null && latitude != null && longitude != null) {
                    locationEditText.setText(address)
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
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                }
            }
            GALLERY_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                }
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
    }
}
