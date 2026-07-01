package com.poke.dex.presentation.profile

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.core.content.edit
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    var username by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var imageUri by mutableStateOf<Uri?>(null)
        private set

    val isUsernameValid: Boolean
        get() = username.isNotBlank() && username.length >= 3

    val isEmailValid: Boolean
        get() = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    val isFormValid: Boolean
        get() = isUsernameValid && isEmailValid

    private val prefs = context.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE)

    fun updateUsername(input: String) { username = input }
    fun updateEmail(input: String) { email = input }
    fun updateImageUri(uri: Uri?) { imageUri = uri }

    fun loadProfile() {
        username = prefs.getString("username", "") ?: ""
        email = prefs.getString("email", "") ?: ""
        val savedUriString = prefs.getString("image_uri", "")
        imageUri = if (!savedUriString.isNullOrEmpty()) savedUriString.toUri() else null
    }

    fun saveProfile(): Boolean {
        if (!isFormValid) return false

        val uriToSave = imageUri
        if (uriToSave != null){
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uriToSave, takeFlags)
            } catch(e: SecurityException) {
                e.printStackTrace()
            }
        }

        prefs.edit(commit = true) {
            putString("username", username)
            putString("email", email)
            putString("image_uri", uriToSave?.toString())
        }
        return true
    }
}
