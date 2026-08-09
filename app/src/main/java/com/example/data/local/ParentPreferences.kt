package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlin.random.Random

class ParentPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("parent_settings_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PARENT_PIN = "parent_pin"
        private const val DEFAULT_PIN = "1234"
        private const val KEY_FAMILY_CODE = "family_code"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_ACTIVE_PROFILE_ID = "active_profile_id"
        private const val KEY_APP_LANGUAGE = "app_language"
        private const val KEY_APP_THEME = "app_theme"
    }

    fun getAppLanguage(): String {
        return prefs.getString(KEY_APP_LANGUAGE, "fr") ?: "fr"
    }

    fun setAppLanguage(lang: String) {
        prefs.edit().putString(KEY_APP_LANGUAGE, lang).apply()
    }

    fun getAppTheme(): String {
        return prefs.getString(KEY_APP_THEME, "system") ?: "system"
    }

    fun setAppTheme(theme: String) {
        prefs.edit().putString(KEY_APP_THEME, theme).apply()
    }

    fun getActiveProfileId(): Long {
        return prefs.getLong(KEY_ACTIVE_PROFILE_ID, 0L)
    }

    fun setActiveProfileId(profileId: Long) {
        prefs.edit().putLong(KEY_ACTIVE_PROFILE_ID, profileId).apply()
    }

    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun getParentPin(): String {
        return prefs.getString(KEY_PARENT_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
    }

    fun setParentPin(newPin: String): Boolean {
        if (newPin.length == 4 && newPin.all { it.isDigit() }) {
            prefs.edit().putString(KEY_PARENT_PIN, newPin).apply()
            return true
        }
        return false
    }

    fun verifyPin(pinInput: String): Boolean {
        return getParentPin() == pinInput
    }

    fun getFamilyCode(): String {
        val existing = prefs.getString(KEY_FAMILY_CODE, null)
        if (existing == null || existing.length != 6 || !existing.all { it.isDigit() }) {
            val newCode = generateRandom6DigitCode()
            prefs.edit().putString(KEY_FAMILY_CODE, newCode).apply()
            return newCode
        }
        return existing
    }

    fun setFamilyCode(code: String): Boolean {
        if (code.length == 6 && code.all { it.isDigit() }) {
            prefs.edit().putString(KEY_FAMILY_CODE, code).apply()
            return true
        }
        return false
    }

    fun generateNewFamilyCode(): String {
        val newCode = generateRandom6DigitCode()
        prefs.edit().putString(KEY_FAMILY_CODE, newCode).apply()
        return newCode
    }

    private fun generateRandom6DigitCode(): String {
        val number = Random.nextInt(100000, 999999)
        return number.toString()
    }
}
