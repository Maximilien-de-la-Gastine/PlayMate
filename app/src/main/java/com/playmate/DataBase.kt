package com.playmate

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.playmate.ui.add_event.Event

class DataBase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "MarkerDatabase.db"
        const val TABLE_NAME = "markers"
        const val COLUMN_MARKER_ID = "marker_id"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_EVENT_NAME = "event_name"
        const val COLUMN_SPORT = "sport"
        const val COLUMN_DATE = "date"
        const val COLUMN_TIME = "time"
        const val COLUMN_DURATION = "duration"
        const val COLUMN_MAX_PEOPLE = "max_people"
        const val COLUMN_REQUIRED_EQUIPMENT = "required_equipment"
        const val COLUMN_REQUIRED_LEVEL = "required_level"
        const val COLUMN_PARTICIPATING = "participating"
        const val COLUMN_USER_NAME = "user_name"

        const val TABLE_AUTH = "users"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_PASSWORD = "password"

        const val TABLE_NAME_LOGGED_IN_USER = "logged_in_user"

        const val TABLE_USER_EVENT = "user_event"



    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_MARKER_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_LATITUDE REAL," +
                "$COLUMN_LONGITUDE REAL," +
                "$COLUMN_EVENT_NAME TEXT," +
                "$COLUMN_SPORT TEXT," +
                "$COLUMN_DATE TEXT," +
                "$COLUMN_TIME TEXT," +
                "$COLUMN_DURATION INTEGER," +
                "$COLUMN_MAX_PEOPLE INTEGER," +
                "$COLUMN_REQUIRED_EQUIPMENT TEXT," +
                "$COLUMN_REQUIRED_LEVEL TEXT," +
                "$COLUMN_PARTICIPATING INTEGER DEFAULT 0," +
                "$COLUMN_USER_NAME TEXT" +
                ")"

        db.execSQL(createTableQuery)

        val createAuthTable = "CREATE TABLE $TABLE_AUTH (" +
                "$COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_USERNAME TEXT," +
                "$COLUMN_PASSWORD TEXT" +
                ")"
        db.execSQL(createAuthTable)

        val createLoggedInUserTableQuery = "CREATE TABLE $TABLE_NAME_LOGGED_IN_USER (" +
                "$COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_USERNAME TEXT" +
                ")"
        db.execSQL(createLoggedInUserTableQuery)

        val createUserEventsTable = "CREATE TABLE $TABLE_USER_EVENT (" +
                "$COLUMN_MARKER_ID INTEGER," +
                "$COLUMN_USER_ID INTEGER," +
                "$COLUMN_USERNAME TEXT," +
                "FOREIGN KEY(marker_id) REFERENCES YourEventTable(marker_id)," +
                "FOREIGN KEY(user_id) REFERENCES YourUserTable(marker_id)" +
                ")"

        db.execSQL(createUserEventsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_AUTH")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_LOGGED_IN_USER")
        onCreate(db)
    }

    fun addMarkerWithDetails(
        latitude: Double,
        longitude: Double,
        eventName: String,
        sport: String,
        date: String,
        time: String,
        duration: Int,
        maxPeople: Int,
        requiredEquipment: String,
        requiredLevel: String,
        participating: Int,
        userName: String
        // ... Add parameters for other details from the form
    ): Long {
        val values = ContentValues()
        values.put(COLUMN_LATITUDE, latitude)
        values.put(COLUMN_LONGITUDE, longitude)
        values.put(COLUMN_EVENT_NAME, eventName)
        values.put(COLUMN_SPORT, sport)
        values.put(COLUMN_DATE, date)
        values.put(COLUMN_TIME, time)
        values.put(COLUMN_DURATION, duration)
        values.put(COLUMN_MAX_PEOPLE, maxPeople)
        values.put(COLUMN_REQUIRED_EQUIPMENT, requiredEquipment)
        values.put(COLUMN_REQUIRED_LEVEL, requiredLevel)
        values.put(COLUMN_PARTICIPATING, participating)
        values.put(COLUMN_USER_NAME, userName)
        // ... Put other details into ContentValues

        val db = this.writableDatabase
        return db.insert(TABLE_NAME, null, values)
    }

    fun getAllMarkers(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }

    fun getMarkersByUsername(userName: String): Cursor {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_USER_NAME = ?"
        return db.rawQuery(query, arrayOf(userName))
    }
    fun getCurrentUsername(): String {
        val db = readableDatabase
        var username: String = "" // Valeur par défaut si le nom d'utilisateur n'est pas trouvé

        val cursor = db.rawQuery("SELECT $COLUMN_USERNAME FROM $TABLE_NAME_LOGGED_IN_USER", null)
        cursor.use {
            if (it.moveToFirst()) {
                username = it.getString(it.getColumnIndexOrThrow(COLUMN_USERNAME))
            }
        }
        cursor.close()
        return username
    }

    fun getCurrentParticipation(markerId: Double): Int {
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_PARTICIPATING FROM $TABLE_NAME WHERE $COLUMN_MARKER_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(markerId.toString()))

        var participation = 0
        cursor.use {
            if (it.moveToFirst()) {
                participation = it.getInt(it.getColumnIndexOrThrow(COLUMN_PARTICIPATING))
            }
        }
        cursor.close()

        return participation
    }

    fun updateParticipation(markerId: Double, newParticipation: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_PARTICIPATING, newParticipation)

        val updatedRows = db.update(
            TABLE_NAME,
            values,
            "$COLUMN_MARKER_ID = ?",
            arrayOf(markerId.toString())
        )

        return updatedRows > 0
    }

    fun getMaxPeople(markerId: Double): Int {
        val db = this.readableDatabase
        var maxPeople = 0

        val query = "SELECT $COLUMN_MAX_PEOPLE FROM $TABLE_NAME WHERE $COLUMN_MARKER_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(markerId.toString()))

        cursor.use {
            if (it.moveToFirst()) {
                maxPeople = it.getInt(it.getColumnIndexOrThrow(COLUMN_MAX_PEOPLE))
            }
        }
        cursor.close()

        return maxPeople
    }

    fun getMarkerCreatorUsername(markerId: Double): String {
        val db = this.readableDatabase
        var creatorUsername = ""

        val query = "SELECT $COLUMN_USER_NAME FROM $TABLE_NAME WHERE $COLUMN_MARKER_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(markerId.toString()))

        cursor.use {
            if (it.moveToFirst()) {
                creatorUsername = it.getString(it.getColumnIndexOrThrow(COLUMN_USER_NAME))
            }
        }
        cursor.close()

        return creatorUsername
    }

    fun getEventDetails(markerId: Double): Event {
        val db = this.readableDatabase
        val eventDetails = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_MARKER_ID = ?", arrayOf(markerId.toString()))

        var event = Event("", "", "", "", 0, 0, "", "", 0.0, 0.0)
        if (eventDetails.moveToFirst()) {
            val eventName = eventDetails.getString(eventDetails.getColumnIndexOrThrow(COLUMN_EVENT_NAME))
            val sport = eventDetails.getString(eventDetails.getColumnIndexOrThrow(COLUMN_SPORT))
            val date = eventDetails.getString(eventDetails.getColumnIndexOrThrow(COLUMN_DATE))
            val time = eventDetails.getString(eventDetails.getColumnIndexOrThrow(COLUMN_TIME))
            val duration = eventDetails.getInt(eventDetails.getColumnIndexOrThrow(COLUMN_DURATION))
            val maxPeople = eventDetails.getInt(eventDetails.getColumnIndexOrThrow(COLUMN_MAX_PEOPLE))
            val requiredEquipment = eventDetails.getString(eventDetails.getColumnIndexOrThrow(COLUMN_REQUIRED_EQUIPMENT))
            val requiredLevel = eventDetails.getString(eventDetails.getColumnIndexOrThrow(COLUMN_REQUIRED_LEVEL))
            val latitude = eventDetails.getDouble(eventDetails.getColumnIndexOrThrow(COLUMN_LATITUDE))
            val longitude = eventDetails.getDouble(eventDetails.getColumnIndexOrThrow(COLUMN_LONGITUDE))

            event = Event(eventName, sport, date, time, duration, maxPeople, requiredEquipment, requiredLevel, latitude, longitude)
        }

        eventDetails.close()
        return event
    }

    fun updateEvent(markerId: Double, event: Event): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("event_name", event.eventName)
        contentValues.put("sport", event.sport)
        contentValues.put("date", event.date)
        contentValues.put("time", event.time)
        contentValues.put("duration", event.duration)
        contentValues.put("max_people", event.maxPeople)
        contentValues.put("required_equipment", event.requiredEquipment)
        contentValues.put("required_level", event.requiredLevel)
        contentValues.put("latitude", event.latitude)
        contentValues.put("longitude", event.longitude)

        val updated = db.update(TABLE_NAME, contentValues, "$COLUMN_MARKER_ID = ?", arrayOf(markerId.toString()))
        return updated > 0
    }

    fun deleteMarker(markerId: Double): Boolean {
        val db = this.writableDatabase
        val deletedRows = db.delete(TABLE_NAME, "$COLUMN_MARKER_ID = ?", arrayOf(markerId.toString()))
        return deletedRows > 0
    }






    //------------------------------------CONNECTION---------------------------------------------------------
    fun registerUser(username: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USERNAME, username)
        values.put(COLUMN_PASSWORD, password)

        val newRowId = db.insert(TABLE_AUTH, null, values)
        return newRowId != -1L // Si l'insertion a réussi, newRowId est l'ID de la nouvelle ligne, sinon -1
    }

    fun isLoggedIn(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_AUTH WHERE $COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(username, password))

        val count = cursor.count
        cursor.close()

        return count > 0
    }

    fun isUserExists(username: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_AUTH WHERE $COLUMN_USERNAME = ?"
        val cursor = db.rawQuery(query, arrayOf(username))
        val count = cursor.count
        cursor.close()
        return count > 0
    }

    fun saveLoggedInUser(username: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USERNAME, username)
        db.insert(TABLE_NAME_LOGGED_IN_USER, null, values)
        db.close()
    }

    fun isUserLoggedIn(): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME_LOGGED_IN_USER"
        val cursor = db.rawQuery(query, null)
        val isLoggedIn = cursor.count > 0
        cursor.close()
        return isLoggedIn
    }

    fun clearLoggedInUser() {
        val db = writableDatabase
        db.delete(TABLE_NAME_LOGGED_IN_USER, null, null)
        db.close()
    }
}

