package com.example.dog_rider_login.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_user_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveUserSession(email: String, name: String, lastName: String, phone: String, isWalker: Boolean) {
        prefs.edit().apply {
            putString("user_email", email)
            putString("user_name", name)
            putString("user_lastname", lastName)
            putString("user_phone", phone)
            putBoolean("user_is_walker", isWalker)
            apply()
        }
    }

    fun getUserEmail(): String? = prefs.getString("user_email", null)
    fun getUserName(): String? = prefs.getString("user_name", "")
    fun getUserLastName(): String? = prefs.getString("user_lastname", "")
    fun getUserPhone(): String? = prefs.getString("user_phone", "")
    fun isWalker(): Boolean = prefs.getBoolean("user_is_walker", false)

    fun logout() {
        prefs.edit().clear().apply()
    }
}
