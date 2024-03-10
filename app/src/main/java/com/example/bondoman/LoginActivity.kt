package com.example.bondoman

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.bondoman.repository.Repository
import com.example.bondoman.storage.TokenManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize view
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)

        // Set click listener for the login button
        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            // Perform login action
            performLogin(email, password)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun performLogin(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val token = Repository().login(email, password)
                    TokenManager.saveToken(this@LoginActivity, token)
                    onLoginSuccess()
                } catch (e: Exception) {
                    showToast("Login failed: ${e.message}")
                }
            }
        } else {
            showToast("Please enter email and password")
        }
    }

    private fun onLoginSuccess() {
        // Start main activity or navigate to main page
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
