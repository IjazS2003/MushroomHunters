package com.example.mushroomhunters.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.mushroomhunters.helper.Utility
import com.example.mushroomhunters.helper.ViewModels

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "mushroom_hunters.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE Trip_TAB (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "date TEXT NOT NULL, " +
                    "time TEXT NOT NULL, " +
                    "location TEXT NOT NULL, " +
                    "duration TEXT NOT NULL, " +
                    "description TEXT, " +
                    "longitude REAL, " +
                    "latitude REAL, " +
                    "image TEXT)"
        )

        db.execSQL(
            "CREATE TABLE Mushroom_TAB (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "tripId INTEGER NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "location TEXT NOT NULL, " +
                    "quantity INTEGER NOT NULL, " +
                    "comments TEXT, " +
                    "longitude REAL, " +
                    "latitude REAL, " +
                    "image TEXT, " +
                    "FOREIGN KEY(tripId) REFERENCES Trip_TAB(id) ON DELETE CASCADE)"
        )

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Mushroom_TAB")
        db.execSQL("DROP TABLE IF EXISTS Trip_TAB")
        onCreate(db)
    }

    fun addTrip(
        name: String,
        date: String,
        time: String,
        location: String,
        duration: String,
        description: String?,
        longitude: Double?,
        latitude: Double?,
        image: String?
    ) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("date", date)
            put("time", time)
            put("location", location)
            put("duration", duration)
            put("description", description)
            put("longitude", longitude)
            put("latitude", latitude)
            put("image", image)
        }
        db.insert("Trip_TAB", null, values)
        db.close()
    }

    fun updateTrip(
        tripId: Int,
        name: String,
        date: String,
        time: String,
        location: String,
        duration: String,
        description: String?,
        longitude: Double?,
        latitude: Double?,
        image: String?
    ) {
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT image FROM Trip_TAB WHERE id=?", arrayOf(tripId.toString()))
        val utility = Utility()
        var already: String? = null

        if (cursor.moveToFirst()) {
            already = cursor.getString(cursor.getColumnIndexOrThrow("image"))
        }
        cursor.close()

        if (already != null && image != null && already != image) {
            utility.deleteFile(already)
        }

        val values = ContentValues().apply {
            put("name", name)
            put("date", date)
            put("time", time)
            put("location", location)
            put("duration", duration)
            put("description", description)
            put("longitude", longitude)
            put("latitude", latitude)
            put("image", image)
        }

        db.update("Trip_TAB", values, "id = ?", arrayOf(tripId.toString()))
        db.close()
    }

    fun deleteTrip(tripId: Int) {
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT image FROM Trip_TAB WHERE id=?", arrayOf(tripId.toString()))
        val utility = Utility()
        var already: String? = null

        if (cursor.moveToFirst()) {
            already = cursor.getString(cursor.getColumnIndexOrThrow("image"))
        }
        cursor.close()

        if (already != null) {
            utility.deleteFile(already)
        }

        db.delete("Trip_TAB", "id = ?", arrayOf(tripId.toString()))
        db.close()
    }

    fun fetchTripById(tripId: Int): ViewModels.Trips? {
        val db = this.readableDatabase
        var tripItem: ViewModels.Trips? = null
        val query = "SELECT * FROM Trip_TAB WHERE id = ?"
        val cursor = db.rawQuery(query, arrayOf(tripId.toString()))

        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
            val time = cursor.getString(cursor.getColumnIndexOrThrow("time"))
            val location = cursor.getString(cursor.getColumnIndexOrThrow("location"))
            val duration = cursor.getString(cursor.getColumnIndexOrThrow("duration"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"))
            val latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"))
            val image = cursor.getString(cursor.getColumnIndexOrThrow("image"))
            val mushroomItems = fetchMushroomsForTrip(tripId)

            tripItem = ViewModels.Trips(
                tripId,
                name,
                date,
                time,
                location,
                duration,
                description,
                longitude,
                latitude,
                image,
                mushroomItems
            )
        }

        cursor.close()
        db.close()
        return tripItem
    }

    fun fetchAllTrips(): List<ViewModels.Trips> {
        val tripItems = mutableListOf<ViewModels.Trips>()
        val db = this.readableDatabase
        val query = "SELECT * FROM Trip_TAB ORDER BY date DESC"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val tripId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val mushroomItems = fetchMushroomsForTrip(tripId)
                val tripItem = ViewModels.Trips(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("date")),
                    cursor.getString(cursor.getColumnIndexOrThrow("time")),
                    cursor.getString(cursor.getColumnIndexOrThrow("location")),
                    cursor.getString(cursor.getColumnIndexOrThrow("duration")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
                    cursor.getString(cursor.getColumnIndexOrThrow("image")),
                    mushroomItems
                )
                tripItems.add(tripItem)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return tripItems
    }
    fun fetchRecentTrips(): List<ViewModels.Trips> {
        val tripItems = mutableListOf<ViewModels.Trips>()
        val db = this.readableDatabase
        // Modify the query to return only the last 5 trips, ordered by date in descending order
        val query = "SELECT * FROM Trip_TAB ORDER BY date DESC LIMIT 5"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val tripId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val mushroomItems = fetchMushroomsForTrip(tripId)
                val tripItem = ViewModels.Trips(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("date")),
                    cursor.getString(cursor.getColumnIndexOrThrow("time")),
                    cursor.getString(cursor.getColumnIndexOrThrow("location")),
                    cursor.getString(cursor.getColumnIndexOrThrow("duration")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
                    cursor.getString(cursor.getColumnIndexOrThrow("image")),
                    mushroomItems
                )
                tripItems.add(tripItem)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return tripItems
    }

    fun fetchMushroomsForTrip(tripId: Int): List<ViewModels.Mushrooms> {
        val mushroomItems = mutableListOf<ViewModels.Mushrooms>()
        val db = this.readableDatabase
        val query = "SELECT * FROM Mushroom_TAB WHERE tripId = ?"
        val cursor = db.rawQuery(query, arrayOf(tripId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val mushroomItem = ViewModels.Mushrooms(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    tripId,
                    cursor.getString(cursor.getColumnIndexOrThrow("type")),
                    cursor.getString(cursor.getColumnIndexOrThrow("location")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                    cursor.getString(cursor.getColumnIndexOrThrow("comments")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
                    cursor.getString(cursor.getColumnIndexOrThrow("image")),
                    null
                )
                mushroomItems.add(mushroomItem)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return mushroomItems
    }

    fun fetchAllMushrooms(): List<ViewModels.Mushrooms> {
        val mushroomItems = mutableListOf<ViewModels.Mushrooms>()
        val db = this.readableDatabase
        val query = "SELECT m.id AS mushroomId, m.tripId, m.type, m.location AS mushroomLocation, " +
                "m.quantity, m.comments, m.longitude AS mushroomLongitude, m.latitude AS mushroomLatitude, m.image AS mushroomImage, " +
                "t.id AS tripId, t.name, t.date, t.time, t.location AS tripLocation, " +
                "t.duration, t.description, t.longitude AS tripLongitude, t.latitude AS tripLatitude, t.image AS tripImage " +
                "FROM Mushroom_TAB m " +
                "INNER JOIN Trip_TAB t ON m.tripId = t.id " +
                "ORDER BY t.date DESC"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val mushroomItem = ViewModels.Mushrooms(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("mushroomId")),
                    tripId = cursor.getInt(cursor.getColumnIndexOrThrow("tripId")),
                    type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                    location = cursor.getString(cursor.getColumnIndexOrThrow("mushroomLocation")),
                    quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                    comments = cursor.getString(cursor.getColumnIndexOrThrow("comments")),
                    longitude = if (!cursor.isNull(cursor.getColumnIndexOrThrow("mushroomLongitude"))) cursor.getDouble(
                        cursor.getColumnIndexOrThrow("mushroomLongitude")
                    ) else null,
                    latitude = if (!cursor.isNull(cursor.getColumnIndexOrThrow("mushroomLatitude"))) cursor.getDouble(
                        cursor.getColumnIndexOrThrow("mushroomLatitude")
                    ) else null,
                    image = cursor.getString(cursor.getColumnIndexOrThrow("mushroomImage")),
                    trip = ViewModels.Trips(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("tripId")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                        time = cursor.getString(cursor.getColumnIndexOrThrow("time")),
                        location = cursor.getString(cursor.getColumnIndexOrThrow("tripLocation")),
                        duration = cursor.getString(cursor.getColumnIndexOrThrow("duration")),
                        description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        longitude = if (!cursor.isNull(cursor.getColumnIndexOrThrow("tripLongitude"))) cursor.getDouble(
                            cursor.getColumnIndexOrThrow("tripLongitude")
                        ) else null,
                        latitude = if (!cursor.isNull(cursor.getColumnIndexOrThrow("tripLatitude"))) cursor.getDouble(
                            cursor.getColumnIndexOrThrow("tripLatitude")
                        ) else null,
                        image = cursor.getString(cursor.getColumnIndexOrThrow("tripImage")),
                        mushrooms = emptyList()
                    )
                )
                mushroomItems.add(mushroomItem)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return mushroomItems
    }
    fun fetchMushroomById(mushroomId: Int): ViewModels.Mushrooms? {
        val db = this.readableDatabase
        var mushroomItem: ViewModels.Mushrooms? = null
        val query = "SELECT * FROM Mushroom_TAB WHERE id = ?"
        val cursor = db.rawQuery(query, arrayOf(mushroomId.toString()))

        if (cursor.moveToFirst()) {
            val type = cursor.getString(cursor.getColumnIndexOrThrow("type"))
            val tripId = cursor.getInt(cursor.getColumnIndexOrThrow("tripId"))
            val qty = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
            val location = cursor.getString(cursor.getColumnIndexOrThrow("location"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("comments"))
            val longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"))
            val latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"))
            val image = cursor.getString(cursor.getColumnIndexOrThrow("image"))

            mushroomItem = ViewModels.Mushrooms(
                id = mushroomId,
                tripId = tripId,
                type = type,
                quantity = qty,
                location = location,
                comments = description,
                longitude = longitude,
                latitude = latitude,
                image = image,
                trip = null
            )
        }

        cursor.close()
        db.close()
        return mushroomItem
    }
    fun addMushroom(
        tripId: Int,
        type: String,
        location: String,
        quantity: Int,
        comments: String?,
        longitude: Double?,
        latitude: Double?,
        image: String?
    ) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("tripId", tripId)
            put("type", type)
            put("location", location)
            put("quantity", quantity)
            put("comments", comments)
            put("longitude", longitude)
            put("latitude", latitude)
            put("image", image)
        }
        db.insert("Mushroom_TAB", null, values)
        db.close()
    }

    fun editMushroom(
        mushroomId: Int,
        tripId: Int,
        type: String,
        location: String,
        quantity: Int,
        comments: String?,
        longitude: Double?,
        latitude: Double?,
        image: String?
    ) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("tripId", tripId)
            put("type", type)
            put("location", location)
            put("quantity", quantity)
            put("comments", comments)
            put("longitude", longitude)
            put("latitude", latitude)
            put("image", image)
        }

        db.update("Mushroom_TAB", values, "id = ?", arrayOf(mushroomId.toString()))
        db.close()
    }
    fun removeMushroom(mushroomId: Int) {
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT image FROM Mushroom_TAB WHERE id=?", arrayOf(mushroomId.toString()))
        val utility = Utility()
        var already: String? = null

        if (cursor.moveToFirst()) {
            already = cursor.getString(cursor.getColumnIndexOrThrow("image"))
        }
        cursor.close()

        if (already != null) {
            utility.deleteFile(already)
        }

        db.delete("Mushroom_TAB", "id = ?", arrayOf(mushroomId.toString()))
        db.close()
    }

fun Populatewithsample(){
    val trips = listOf(
        ViewModels.Trips(1,"Mushroom Hunt in Forest Park", "2024-11-23", "10:00", "Forest Park", "2 hours", "Exploring mushrooms in the forest.", 40.7128, -74.0060, "forest_park_image.jpg",mushrooms = null),
        ViewModels.Trips(2,"Spring Meadow Mushroom Search", "2024-11-24", "09:00", "Spring Meadow", "3 hours", "Hunting for wild mushrooms at Spring Meadow.", 40.7580, -73.9855, "spring_meadow_image.jpg",mushrooms = null),
        ViewModels.Trips(3,"Mountain Ridge Fungi Hunt", "2024-11-25", "11:00", "Mountain Ridge", "4 hours", "Searching for rare fungi in the mountains.", 39.7392, -104.9903, "mountain_ridge_image.jpg",mushrooms = null)
    )

    val db = this.writableDatabase
    for (trip in trips) {
        val values = ContentValues().apply {
            put("name", trip.name)
            put("date", trip.date)
            put("time", trip.time)
            put("location", trip.location)
            put("duration", trip.duration)
            put("description", trip.description)
            put("longitude", trip.longitude)
            put("latitude", trip.latitude)
            put("image", trip.image)
        }
        db.insert("Trip_TAB", null, values)
    }
    val mushrooms = listOf(
        ViewModels.Mushrooms(1,
            1,
            "Chanterelle",
            "Near the oak tree",
            5,
            "Found growing under the oak tree in the forest.",
            40.7128,
            -74.0060,
            "chanterelle_image.jpg",trip = null
        ),
        ViewModels.Mushrooms(2,1, "Boletus", "Near the stream", 3, "A small patch near the stream.", 40.7130, -74.0065, "boletus_image.jpg",trip = null),
        ViewModels.Mushrooms(3,2, "Morel", "In the meadow", 8, "Spotted some morels growing in the meadow.", 40.7580, -73.9855, "morel_image.jpg",trip = null),
        ViewModels.Mushrooms(4,2, "Shiitake", "Forest edge", 4, "Found a few shiitakes near the edge of the forest.", 40.7585, -73.9858, "shiitake_image.jpg",trip = null),
        ViewModels.Mushrooms(5,3, "Porcini", "Under the pine tree", 2, "A couple of porcini mushrooms found under the pine.", 39.7392, -104.9903, "porcini_image.jpg",trip = null)
    )

    for (mushroom in mushrooms) {
        val values = ContentValues().apply {
            put("tripId", mushroom.tripId)
            put("type", mushroom.type)
            put("location", mushroom.location)
            put("quantity", mushroom.quantity)
            put("comments", mushroom.comments)
            put("longitude", mushroom.longitude)
            put("latitude", mushroom.latitude)
            put("image", mushroom.image)
        }
        db.insert("Mushroom_TAB", null, values)
    }
    db.close()
}
}
