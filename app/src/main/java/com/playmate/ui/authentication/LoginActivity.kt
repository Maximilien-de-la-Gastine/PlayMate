package com.playmate.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.playmate.MainActivity
import com.playmate.R
import com.playmate.DataBase

class LoginActivity : AppCompatActivity() {

    private lateinit var buttonLogin: Button
    private lateinit var buttonRegister: Button
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextNewUsername: EditText
    private lateinit var editTextNewPassword: EditText
    private lateinit var userDBHelper: DataBase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userDBHelper = DataBase (this)

        if(userDBHelper.isUserLoggedIn()){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        else{
            buttonLogin = findViewById(R.id.buttonLogin)
            editTextUsername = findViewById(R.id.editTextUsername)
            editTextPassword = findViewById(R.id.editTextPassword)
            buttonRegister = findViewById(R.id.buttonRegister)
            editTextNewUsername = findViewById(R.id.editTextNewUsername)
            editTextNewPassword = findViewById(R.id.editTextNewPassword)

            buttonLogin.setOnClickListener {
                val username = editTextUsername.text.toString()
                val password = editTextPassword.text.toString()

                if (userDBHelper.isLoggedIn(username, password)) {
                    showToast("Connexion réussie en tant que $username")
                    userDBHelper.saveLoggedInUser(username)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    showToast("Échec de la connexion. Veuillez vérifier vos informations.")
                }
            }

            buttonRegister.setOnClickListener {
                val newUsername = editTextNewUsername.text.toString()
                val newPassword = editTextNewPassword.text.toString()

                if (newUsername.isNotEmpty() && newPassword.isNotEmpty()) {
                    val isUserExists = userDBHelper.isUserExists(newUsername)

                    if (isUserExists) {
                        showToast("Username already exists. Please choose another one.")
                    } else {
                        val isRegistered = userDBHelper.registerUser(newUsername, newPassword)
                        if (isRegistered) {
                            showToast("Registration successful")
                        } else {
                            showToast("Registration failed. Please try again.")
                        }
                    }
                } else {
                    showToast("Please fill in all fields")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

