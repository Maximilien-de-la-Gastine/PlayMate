package com.playmate.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.playmate.MainActivity
import com.playmate.R
import com.playmate.UserDBHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var buttonLogin: Button
    private lateinit var buttonRegister: Button
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextNewUsername: EditText
    private lateinit var editTextNewPassword: EditText
    private lateinit var userDBHelper: UserDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        buttonLogin = findViewById(R.id.buttonLogin)
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        userDBHelper = UserDBHelper(this)

        buttonLogin.setOnClickListener {
            val username = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()

            if (userDBHelper.isLoggedIn(username, password)) {
                showToast("Connexion réussie en tant que $username")
                // Redirection vers l'écran suivant après la connexion réussie
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                showToast("Échec de la connexion. Veuillez vérifier vos informations.")
            }
        }

        buttonRegister = findViewById(R.id.buttonRegister)
        editTextNewUsername = findViewById(R.id.editTextNewUsername)
        editTextNewPassword = findViewById(R.id.editTextNewPassword)
        userDBHelper = UserDBHelper(this)

        buttonRegister.setOnClickListener {
            val newUsername = editTextNewUsername.text.toString()
            val newPassword = editTextNewPassword.text.toString()

            if (newUsername.isNotEmpty() && newPassword.isNotEmpty()) {
                // Vérifiez si l'utilisateur existe déjà dans la base de données
                val isUserExists = userDBHelper.isUserExists(newUsername)

                if (isUserExists) {
                    showToast("Username already exists. Please choose another one.")
                } else {
                    val isRegistered = userDBHelper.registerUser(newUsername, newPassword)
                    if (isRegistered) {
                        showToast("Registration successful")
                        // Redirigez l'utilisateur vers la connexion ou une autre activité si nécessaire
                    } else {
                        showToast("Registration failed. Please try again.")
                    }
                }
            } else {
                showToast("Please fill in all fields")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

