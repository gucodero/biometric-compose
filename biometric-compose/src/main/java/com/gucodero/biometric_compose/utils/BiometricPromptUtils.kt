package com.gucodero.biometric_compose.utils

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.gucodero.biometric_compose.entities.AllowedAuthenticators

object LocalFragmentActivity {
    val current @Composable get(): FragmentActivity? {
        val context = LocalContext.current
        return remember(context) {
            when(context){
                is FragmentActivity -> context
                else -> null
            }
        }
    }

}

@Composable
fun isBiometricPromptIsAvailable(allowedAuthenticators: AllowedAuthenticators): Boolean {
    val activity = LocalFragmentActivity.current ?: return false
    if(BiometricManager.from(activity.applicationContext).canAuthenticate(allowedAuthenticators.value) != BiometricManager.BIOMETRIC_SUCCESS){
        return false
    }
    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
        return false
    }
    return true
}



