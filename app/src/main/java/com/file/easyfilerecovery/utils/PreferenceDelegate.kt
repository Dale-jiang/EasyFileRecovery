package com.file.easyfilerecovery.utils

import android.content.Context
import androidx.core.content.edit
import com.file.easyfilerecovery.APP
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class PreferenceDelegate<T>(
    private val key: String?,
    private val defaultValue: T
) : ReadWriteProperty<Any?, T> {

    private val prefs by lazy { APP.app.getSharedPreferences("easy_file_recovery_prefs", Context.MODE_PRIVATE) }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val realKey = key ?: property.name
        return when (defaultValue) {
            is String -> prefs.getString(realKey, defaultValue) as T
            is Int -> prefs.getInt(realKey, defaultValue) as T
            is Boolean -> prefs.getBoolean(realKey, defaultValue) as T
            is Float -> prefs.getFloat(realKey, defaultValue) as T
            is Long -> prefs.getLong(realKey, defaultValue) as T
            is Double -> {
                val bits = prefs.getString(realKey, "0.0")
                (bits?.toDoubleOrNull() ?: 0.0) as T
            }

            else -> throw IllegalArgumentException("This type can't be saved into Preferences")
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val realKey = key ?: property.name
        prefs.edit {
            when (value) {
                is String -> putString(realKey, value)
                is Int -> putInt(realKey, value)
                is Boolean -> putBoolean(realKey, value)
                is Float -> putFloat(realKey, value)
                is Long -> putLong(realKey, value)
                is Double -> putString(realKey, value.toString())
                else -> error("This type can't be saved into Preferences")
            }
        }
    }
}


fun sString(key: String? = null, default: String = "") = PreferenceDelegate(key, default)

fun sInt(key: String? = null, default: Int = 0) = PreferenceDelegate(key, default)

fun sLong(key: String? = null, default: Long = 0L) = PreferenceDelegate(key, default)

fun sFloat(key: String? = null, default: Float = 0f) = PreferenceDelegate(key, default)

fun sBoolean(key: String? = null, default: Boolean = false) = PreferenceDelegate(key, default)

fun sDouble(key: String? = null, default: Double = 0.0) = PreferenceDelegate(key, default)

var firstLaunchTag by sBoolean(default = true)
var isFirstRequestStorage by sBoolean(default = true)


