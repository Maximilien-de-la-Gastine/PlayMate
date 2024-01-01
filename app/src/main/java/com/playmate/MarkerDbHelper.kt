package com.playmate

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.Duration

class MarkerDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "MarkerDatabase.db"
        const val TABLE_NAME = "markers"
        const val COLUMN_ID = "id"
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
        // ... Add more columns for other details in the form
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
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
                "$COLUMN_PARTICIPATING INTEGER DEFAULT 0" +
                ")"
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
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
        participating: Int
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
        // ... Put other details into ContentValues

        val db = this.writableDatabase
        return db.insert(TABLE_NAME, null, values)
    }

    fun getAllMarkers(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }

    fun incrementParticipants(eventId: Long) {
        val db = this.writableDatabase
        val updateQuery = "UPDATE $TABLE_NAME SET $COLUMN_PARTICIPATING = $COLUMN_PARTICIPATING + 1 WHERE $COLUMN_ID = ?"
        db.execSQL(updateQuery, arrayOf(eventId))
    }

    fun getNumberOfParticipants(eventId: Long): Int {
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_PARTICIPATING FROM $TABLE_NAME WHERE $COLUMN_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(eventId.toString()))

        var count = 0
        cursor?.use {
            if (it.moveToFirst()) {
                count = it.getInt(it.getColumnIndexOrThrow(COLUMN_PARTICIPATING))
            }
        }

        cursor?.close()
        return count
    }


}

