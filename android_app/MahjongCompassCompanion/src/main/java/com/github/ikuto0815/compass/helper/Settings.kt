package com.github.ikuto0815.compass.helper

import android.content.SharedPreferences
import androidx.core.content.edit

object Settings {
    var preferences : SharedPreferences? = null

    fun setValue(key: String, value: String) {
        preferences?.edit {
            putString(key, value)
        }
    }

    fun getValue(key: String, default: String? = null): String? {
        return preferences?.getString(key, default)
    }
}