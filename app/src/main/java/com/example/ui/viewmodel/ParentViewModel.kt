package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.local.ParentPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ParentViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = ParentPreferences(application)

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated = _isAuthenticated.asStateFlow()

    private val _pinInput = MutableStateFlow("")
    val pinInput = _pinInput.asStateFlow()

    private val _pinError = MutableStateFlow(false)
    val pinError = _pinError.asStateFlow()

    private val _showPinModal = MutableStateFlow(false)
    val showPinModal = _showPinModal.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(prefs.isBiometricEnabled())
    val isBiometricEnabled = _isBiometricEnabled.asStateFlow()

    private val _appLanguage = MutableStateFlow(prefs.getAppLanguage())
    val appLanguage = _appLanguage.asStateFlow()

    private val _appTheme = MutableStateFlow(prefs.getAppTheme())
    val appTheme = _appTheme.asStateFlow()

    fun setAppLanguage(lang: String) {
        prefs.setAppLanguage(lang)
        _appLanguage.value = lang
    }

    fun setAppTheme(theme: String) {
        prefs.setAppTheme(theme)
        _appTheme.value = theme
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.setBiometricEnabled(enabled)
        _isBiometricEnabled.value = enabled
    }

    fun onBiometricSuccess() {
        _isAuthenticated.value = true
        _showPinModal.value = false
        _pinInput.value = ""
        _pinError.value = false
    }

    fun openPinModal() {
        _pinInput.value = ""
        _pinError.value = false
        _showPinModal.value = true
    }

    fun closePinModal() {
        _pinInput.value = ""
        _pinError.value = false
        _showPinModal.value = false
    }

    fun appendPinDigit(digit: String) {
        if (_pinInput.value.length < 4) {
            val newPin = _pinInput.value + digit
            _pinInput.value = newPin
            _pinError.value = false

            if (newPin.length == 4) {
                if (prefs.verifyPin(newPin)) {
                    _isAuthenticated.value = true
                    _showPinModal.value = false
                    _pinInput.value = ""
                } else {
                    _pinError.value = true
                    _pinInput.value = ""
                }
            }
        }
    }

    fun deletePinDigit() {
        if (_pinInput.value.isNotEmpty()) {
            _pinInput.value = _pinInput.value.dropLast(1)
            _pinError.value = false
        }
    }

    fun updatePin(oldPin: String, newPin: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!prefs.verifyPin(oldPin)) {
            onError("Ancien code PIN incorrect.")
            return
        }
        if (newPin.length != 4 || !newPin.all { it.isDigit() }) {
            onError("Le nouveau code PIN doit contenir exactement 4 chiffres.")
            return
        }
        if (prefs.setParentPin(newPin)) {
            onSuccess()
        } else {
            onError("Erreur lors du changement de PIN.")
        }
    }

    fun logout() {
        _isAuthenticated.value = false
    }
}
