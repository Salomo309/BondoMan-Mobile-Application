package com.example.bondoman.ui.setting

import android.app.Application
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bondoman.TokenExpirationService
import com.example.bondoman.storage.TokenManager
import kotlinx.coroutines.launch

class SettingViewModel(application: Application) : AndroidViewModel(application) {

    private val _text = MutableLiveData<String>().apply {
        value = "This is setting Fragment"
    }
    val text: LiveData<String> = _text

    fun logout(){
        // Invalidate token locally
        invalidateTokenLocally()

        // Stop all services
        val context:Application = getApplication()
        context.stopService(Intent(context, TokenExpirationService::class.java))
    }

    private fun invalidateTokenLocally() {
        viewModelScope.launch {
            TokenManager.saveToken(context = getApplication(), token = "")
        }
    }
}