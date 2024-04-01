package com.example.bondoman

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bondoman.repository.Repository
import com.example.bondoman.service.TokenExpirationService
import com.example.bondoman.storage.TokenManager
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Hide the action bar
        supportActionBar?.hide()

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            performLogin(email, password)
        }
    }

    private fun performLogin(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            coroutineScope.launch {
                try {
                    val token = withContext(Dispatchers.IO) {
                        Repository().login(email, password)
                    }
                    TokenManager.saveToken(this@LoginActivity, token)
                    TokenManager.fetchTokenResponse(this@LoginActivity, token)
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
        startTokenExpirationService()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun startTokenExpirationService() {
        val intent = Intent(this, TokenExpirationService::class.java)
        startService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel() // Cancel coroutine scope to avoid memory leaks
    }
}
