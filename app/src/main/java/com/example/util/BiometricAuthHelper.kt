package com.example.util

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricAuthHelper {

    private const val TAG = "BiometricAuthHelper"

    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun promptBiometric(
        activity: FragmentActivity,
        title: String = "Accès Espace Parent",
        subtitle: String = "Vérifiez votre empreinte ou visage pour continuer",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val executor = ContextCompat.getMainExecutor(activity)
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d(TAG, "Biometric authentication succeeded")
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.w(TAG, "Biometric error [$errorCode]: $errString")
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_CANCELED
                    ) {
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.w(TAG, "Biometric authentication failed")
                    onError("Empreinte / Visage non reconnu. Réessayez.")
                }
            }

            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText("Utiliser le code PIN")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.BIOMETRIC_WEAK
                )
                .build()

            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching biometric prompt", e)
            onError(e.localizedMessage ?: "Authentification biométrique indisponible.")
        }
    }
}
