package com.example.mushroomhunters.helper
class ViewModels {

    data class Mushrooms(
        val id: Int,
        val tripId: Int,
        val type: String,
        val location: String,
        val quantity: Int,
        val comments: String?,
        val longitude: Double?,
        val latitude: Double?,
        val image: String?,
        val trip: Trips?
    )

    data class Trips(
        val id: Int,
        val name: String,
        val date: String,
        val time: String,
        val location: String,
        val duration: String,
        val description: String?,
        val longitude: Double?,
        val latitude: Double?,
        val image: String?,
        val mushrooms: List<Mushrooms>?

    )

}