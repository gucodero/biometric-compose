package com.gucodero.biometric_compose.entities

import androidx.biometric.BiometricManager

enum class AllowedAuthenticators(val value: Int) {
    BIOMETRIC_STRONG(
        value = BiometricManager.Authenticators.BIOMETRIC_STRONG
    ),
    BIOMETRIC_WEAK(
        value = BiometricManager.Authenticators.BIOMETRIC_WEAK
    ),
    DEVICE_CREDENTIAL(
        value = BiometricManager.Authenticators.DEVICE_CREDENTIAL
    )
}