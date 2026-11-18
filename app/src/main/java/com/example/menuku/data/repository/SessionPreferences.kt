package com.example.menuku.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

class SessionPreferences(context: Context) {
    private val preferences = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    val rememberMe: Boolean get() = preferences.getBoolean("remember", false)
    fun remember(value: Boolean) {
        preferences.edit().putBoolean("remember", value).apply()
    }
    fun restore() {
        if (!rememberMe) FirebaseAuth.getInstance().signOut()
    }
    fun signOut() {
        remember(false)
        FirebaseAuth.getInstance().signOut()
    }
}
