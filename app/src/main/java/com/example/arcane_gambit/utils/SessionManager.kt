package com.example.arcane_gambit.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Manages user session data for the Arcane Gambit app
 */
class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    companion object {
        private const val PREF_NAME = "ArcaneGambitPrefs"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USERNAME = "username"
        private const val KEY_RFID_TAG = "rfidTag"
        private const val KEY_USER_TOKEN = "userToken"

        private const val TAG = "SessionManager"
    }

    /**
     * Save user login session
     */
    fun createLoginSession(userId: String, username: String, rfidTag: String? = null, token: String? = null) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_USER_ID, userId)
        editor.putString(KEY_USERNAME, username)

        // Only save RFID tag if provided
        if (rfidTag != null) {
            editor.putString(KEY_RFID_TAG, rfidTag)
        }

        // Save token if provided
        if (token != null) {
            editor.putString(KEY_USER_TOKEN, token)
        }

        // Commit changes
        editor.apply()

        Log.d(TAG, "User login session created for $username with RFID: ${rfidTag != null}")
    }

    /**
     * Update RFID tag for existing user
     */
    fun updateRfidTag(rfidTag: String) {
        editor.putString(KEY_RFID_TAG, rfidTag)
        editor.apply()
        Log.d(TAG, "RFID tag updated: $rfidTag")
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Get user ID
     */
    fun getUserId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, null)
    }

    /**
     * Get username
     */
    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    /**
     * Get saved RFID tag
     */
    fun getRfidTag(): String? {
        return sharedPreferences.getString(KEY_RFID_TAG, null)
    }

    /**
     * Get authentication token
     */
    fun getToken(): String? {
        return sharedPreferences.getString(KEY_USER_TOKEN, null)
    }

    /**
     * Clear session data (logout)
     */
    fun logout() {
        editor.clear()
        editor.apply()
        Log.d(TAG, "User logged out, session cleared")
    }

    /**
     * Quick login with RFID tag
     * Returns true if the tag matches saved tag and auto-login is possible
     */
    fun checkRfidTagForLogin(scannedTag: String): Boolean {
        val savedTag = getRfidTag()

        // If we have a saved tag and it matches the scanned tag
        if (savedTag != null && savedTag == scannedTag) {
            Log.d(TAG, "RFID tag matched existing user")
            return true
        }

        Log.d(TAG, "RFID tag didn't match any saved tag")
        return false
    }
}