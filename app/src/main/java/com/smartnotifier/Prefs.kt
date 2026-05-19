package com.smartnotifier

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("smart_notifier_prefs", Context.MODE_PRIVATE)

    var senderName: String
        get() = prefs.getString(KEY_SENDER_NAME, "") ?: ""
        set(v) = prefs.edit().putString(KEY_SENDER_NAME, v).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION, true)
        set(v) = prefs.edit().putBoolean(KEY_VIBRATION, v).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(v) = prefs.edit().putBoolean(KEY_SOUND, v).apply()

    var volumePercent: Int
        get() = prefs.getInt(KEY_VOLUME, 80)
        set(v) = prefs.edit().putInt(KEY_VOLUME, v).apply()

    var serviceEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, true)
        set(v) = prefs.edit().putBoolean(KEY_ENABLED, v).apply()

    companion object {
        private const val KEY_SENDER_NAME = "sender_name"
        private const val KEY_VIBRATION   = "vibration_enabled"
        private const val KEY_SOUND       = "sound_enabled"
        private const val KEY_VOLUME      = "volume_percent"
        private const val KEY_ENABLED     = "service_enabled"
    }
}
