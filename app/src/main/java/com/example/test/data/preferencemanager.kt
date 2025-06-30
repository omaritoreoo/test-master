package com.example.test.data // Sesuaikan package

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_LOGGED_IN_USER_ID = "logged_in_user_id"
    }

    fun setLoggedInUserId(userId: Long) { // <--- Pastikan ini menerima Long
        prefs.edit().putLong(KEY_LOGGED_IN_USER_ID, userId).apply()
    }

    fun getLoggedInUserId(): Long { // <--- Pastikan ini mengembalikan Long
        return prefs.getLong(KEY_LOGGED_IN_USER_ID, -1L)
    }

    fun clearLoggedInUserId() {
        prefs.edit().remove(KEY_LOGGED_IN_USER_ID).apply()
    }
}