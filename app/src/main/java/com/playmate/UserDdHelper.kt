package com.playmate

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UserDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "UserDatabase.db"
        const val TABLE_NAME = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_PASSWORD = "password"
        const val TABLE_NAME_LOGGED_IN_USER = "logged_in_user"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_USERNAME TEXT," +
                "$COLUMN_PASSWORD TEXT" +
                ")"
        db.execSQL(createTableQuery)

        val createLoggedInUserTableQuery = "CREATE TABLE $TABLE_NAME_LOGGED_IN_USER (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_USERNAME TEXT" +
                ")"
        db.execSQL(createLoggedInUserTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_LOGGED_IN_USER")
        onCreate(db)
    }

    fun registerUser(username: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USERNAME, username)
        values.put(COLUMN_PASSWORD, password)

        val newRowId = db.insert(TABLE_NAME, null, values)
        return newRowId != -1L // Si l'insertion a réussi, newRowId est l'ID de la nouvelle ligne, sinon -1
    }

    fun isLoggedIn(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(username, password))

        val count = cursor.count
        cursor.close()

        return count > 0
    }

    fun isUserExists(username: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_USERNAME = ?"
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



}
