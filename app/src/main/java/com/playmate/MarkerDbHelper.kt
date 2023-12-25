package com.playmate

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

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
        // ... Add more columns for other details in the form
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_LATITUDE REAL," +
                "$COLUMN_LONGITUDE REAL," +
                "$COLUMN_EVENT_NAME TEXT," +
                "$COLUMN_SPORT TEXT" +
                // ... Add more columns here
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
        sport: String
        // ... Add parameters for other details from the form
    ): Long {
        val values = ContentValues()
        values.put(COLUMN_LATITUDE, latitude)
        values.put(COLUMN_LONGITUDE, longitude)
        values.put(COLUMN_EVENT_NAME, eventName)
        values.put(COLUMN_SPORT, sport)
        // ... Put other details into ContentValues

        val db = this.writableDatabase
        return db.insert(TABLE_NAME, null, values)
    }

    fun getAllMarkers(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }
}

