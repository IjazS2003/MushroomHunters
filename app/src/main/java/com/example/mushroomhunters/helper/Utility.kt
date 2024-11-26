package com.example.mushroomhunters.helper

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Utility {

    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val encodedString = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        Log.d("Image Encoding", "Bitmap successfully encoded to Base64")
        return encodedString
    }

    fun base64ToBitmap(base64Data: String): Bitmap {
        val byteArray = Base64.decode(base64Data, Base64.DEFAULT)
        Log.d("Image Decoding", "Base64 string successfully decoded to Bitmap")
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun uriToBase64(imageUri: Uri, contentResolver: ContentResolver): String {
        val inputStream = contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val encodedString = bitmapToBase64(bitmap)
        Log.d("Image Conversion", "Image URI successfully converted to Base64 string")
        return encodedString
    }

    fun saveBitmap(context: Context, bitmap: Bitmap): String? {
        val filename = "mushroom_image_${System.currentTimeMillis()}.jpg"
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(directory, filename)

        var fileOutputStream: FileOutputStream? = null
        return try {
            fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            Log.d("Image Save", "Bitmap successfully saved to: ${file.absolutePath}")
            file.absolutePath
        } catch (e: IOException) {
            Log.e("Image Save Error", "Failed to save bitmap to storage", e)
            null
        } finally {
            fileOutputStream?.close()
        }
    }

    fun loadBitmap(uri: Uri, contentResolver: ContentResolver): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            Log.d("Image Load", "Bitmap successfully loaded from URI")
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            Log.e("Image Load Error", "Failed to load bitmap from URI", e)
            null
        }
    }

    fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        return try {
            if (file.exists()) {
                file.delete()
                Log.d("File Deletion", "File successfully deleted: $filePath")
                true
            } else {
                Log.d("File Deletion", "File not found: $filePath")
                false
            }
        } catch (e: Exception) {
            Log.e("File Deletion Error", "Failed to delete file", e)
            false
        }
    }

    fun bitmapFromFile(filePath: String): Bitmap? {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                Log.d("Bitmap Load", "Bitmap successfully loaded from file: $filePath")
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                Log.d("Bitmap Load", "File not found: $filePath")
                null
            }
        } catch (e: Exception) {
            Log.e("Bitmap Load Error", "Failed to load bitmap from file", e)
            null
        }
    }

    private val networkClient = OkHttpClient()
    private val serverUrl = "https://stuiis.cms.gre.ac.uk/COMP1424CoreWS/comp1424cw"
    private val userId = "001375399"

    @RequiresApi(Build.VERSION_CODES.O)
    fun createTripPayload(tripList: List<ViewModels.Trips>): String {
        val jsonObject = JsonObject()
        jsonObject.addProperty("userId", userId)

        val tripDataList = mutableListOf<Map<String, Any?>>()
        for (trip in tripList) {
            val dateTime = "${trip.date} ${trip.time}"
            val (dayOfWeek, timeFormatted) = formatDateTimeDetails(dateTime)
            val tripDetails = mapOf(
                "name" to trip.name,
                "dateOfTrip" to trip.date,
                "timeOfTrip" to trip.time,
                "location" to trip.location,
                "duration" to trip.duration,
                "dayOfWeek" to dayOfWeek?.name,
                "timeOfDay" to timeFormatted,
                "mushroomList" to trip.mushrooms?.map { mushroom ->
                    mapOf(
                        "type" to mushroom.type,
                        "location" to mushroom.location,
                        "quantity" to mushroom.quantity
                    )
                }
            )
            tripDataList.add(tripDetails)
        }
        jsonObject.add("detailList", Gson().toJsonTree(tripDataList))
        Log.d("Payload Generation", "Trip payload successfully created: ${jsonObject.toString()}")
        return jsonObject.toString()
    }

    fun sendTripData(payload: String, resultCallback: (Boolean, String, Map<String, List<String>>?) -> Unit) {
        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaType(), payload)
        val request = Request.Builder().url(serverUrl).post(requestBody).build()

        networkClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Network Error", "Failed to send trip data to the server", e)
                Handler(Looper.getMainLooper()).post {
                    resultCallback(false, "Network error: ${e.message}", null)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val responseHeaders = response.headers.toMultimap()
                if (response.isSuccessful) {
                    Log.d("Network Success", "Data successfully uploaded: ${responseBody ?: "No response body"}")
                    Handler(Looper.getMainLooper()).post {
                        resultCallback(true, responseBody ?: "No response body", responseHeaders)
                    }
                } else {
                    Log.e("Network Failure", "Server returned error code: ${response.code}")
                    Handler(Looper.getMainLooper()).post {
                        resultCallback(false, "Server error code: ${response.code}", responseHeaders)
                    }
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDateTimeDetails(dateTime: String): Pair<DayOfWeek?, String> {
        val parsedDateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        val dayOfWeek = parsedDateTime.dayOfWeek
        val formattedTime = parsedDateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
        Log.d("Date-Time Formatting", "Formatted day: $dayOfWeek, time: $formattedTime")
        return Pair(dayOfWeek, formattedTime)
    }
}
