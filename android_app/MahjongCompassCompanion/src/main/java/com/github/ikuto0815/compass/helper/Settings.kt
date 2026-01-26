package com.github.ikuto0815.compass.helper

import android.content.SharedPreferences
import androidx.core.content.edit

object Settings {

    val callbacks : MutableMap<String, (String)-> Unit> = mutableMapOf()
    var preferences : SharedPreferences? = null

    fun setValue(key: String, value: String) {
        preferences?.edit {
            putString(key, value)
        }

        callbacks.get(key)?.invoke(value)
    }

    fun getValue(key: String, default: String? = null): String? {
        return preferences?.getString(key, default)
    }

    fun addSetterCallback(key: String, cb: (String) -> Unit) {
        callbacks[key] = cb
    }
}