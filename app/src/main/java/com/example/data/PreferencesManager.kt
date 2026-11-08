package com.example.data

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_hider_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PIN_HASH = "key_pin_hash"
        private const val KEY_DECOY_PIN_HASH = "key_decoy_pin_hash"
        private const val KEY_DISGUISE_MODE = "key_disguise_mode"
        private const val KEY_QUICK_LOCK = "key_quick_lock"
        private const val KEY_FIRST_LAUNCH = "key_first_launch"
        private const val DEFAULT_PIN = "1234"
    }

    init {
        // Initialize default PIN (1234) if not already set
        if (!prefs.contains(KEY_PIN_HASH)) {
            setPin(DEFAULT_PIN)
        }
    }

    private fun hash(text: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(text.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun isPinCorrect(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return pin == DEFAULT_PIN
        return storedHash == hash(pin)
    }

    fun isDecoyPin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_DECOY_PIN_HASH, null) ?: return false
        return storedHash == hash(pin)
    }

    fun setPin(pin: String) {
        prefs.edit().putString(KEY_PIN_HASH, hash(pin)).apply()
    }

    fun setDecoyPin(pin: String?) {
        if (pin.isNullOrBlank()) {
            prefs.edit().remove(KEY_DECOY_PIN_HASH).apply()
        } else {
            prefs.edit().putString(KEY_DECOY_PIN_HASH, hash(pin)).apply()
        }
    }

    fun hasDecoyPin(): Boolean {
        return prefs.contains(KEY_DECOY_PIN_HASH)
    }

    var isDisguiseModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_DISGUISE_MODE, true)
        set(value) = prefs.edit().putBoolean(KEY_DISGUISE_MODE, value).apply()

    var isQuickLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_QUICK_LOCK, true)
        set(value) = prefs.edit().putBoolean(KEY_QUICK_LOCK, value).apply()

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()
}
