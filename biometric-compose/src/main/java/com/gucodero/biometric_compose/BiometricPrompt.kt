package com.gucodero.biometric_compose

import android.annotation.SuppressLint
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.gucodero.biometric_compose.entities.AllowedAuthenticators
import com.gucodero.biometric_compose.entities.CiphertextWrapper
import com.gucodero.biometric_compose.entities.DecryptionResult
import com.gucodero.biometric_compose.entities.EncryptionResult
import com.gucodero.biometric_compose.utils.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

@Composable
fun rememberBiometricPrompt(
    allowedAuthenticators: AllowedAuthenticators,
    title: CharSequence? = null,
    subtitle: CharSequence? = null,
    description: CharSequence? = null,
    confirmationRequired: Boolean? = null,
    negativeButtonText: String? = null
): com.gucodero.biometric_compose.BiometricPrompt {
    val isAvailable = isBiometricPromptIsAvailable(
        allowedAuthenticators = allowedAuthenticators
    )
    return if(isAvailable){
        val activity = LocalFragmentActivity.current!!
        remember {
            val promptInfo = BiometricPrompt.PromptInfo.Builder().apply {
                title?.let(this::setTitle)
                subtitle?.let(this::setSubtitle)
                description?.let(this::setDescription)
                confirmationRequired?.let(this::setConfirmationRequired)
                negativeButtonText?.let(this::setNegativeButtonText)
                setAllowedAuthenticators(allowedAuthenticators.value)
            }.build()
            BiometricPromptAvailable(
                activity = activity,
                promptInfo = promptInfo
            )
        }
    } else {
        remember {
            BiometricPromptNotAvailable()
        }
    }
}

interface BiometricPrompt {

    suspend fun encryption(key: String, value: String): Flow<EncryptionResult>

    suspend fun decryption(key: String, value: CiphertextWrapper): Flow<DecryptionResult>

}

private class BiometricPromptNotAvailable: com.gucodero.biometric_compose.BiometricPrompt {

    override suspend fun decryption(key: String, value: CiphertextWrapper): Flow<DecryptionResult> = flow {
        emit(DecryptionResult.NotAvailable)
    }

    override suspend fun encryption(key: String, value: String): Flow<EncryptionResult> = flow {
        emit(EncryptionResult.NotAvailable)
    }

}

private class BiometricPromptAvailable(
    private val activity: FragmentActivity,
    private val promptInfo: BiometricPrompt.PromptInfo
): com.gucodero.biometric_compose.BiometricPrompt {

    @SuppressLint("NewApi")
    override suspend fun encryption(key: String, value: String): Flow<EncryptionResult> = callbackFlow {
        val cipher = CipherEncryption(
            keyName = key
        )
        val executor = ContextCompat.getMainExecutor(activity.baseContext)
        val callback = object : BiometricPrompt.AuthenticationCallback(){

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if(errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON){
                    trySend(EncryptionResult.Cancel)
                } else {
                    trySend(
                        EncryptionResult.Error(
                            errorCode = errorCode,
                            description = errString.toString()
                        )
                    )
                }
                cancel()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                trySend(EncryptionResult.Failed)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                result.cryptoObject?.cipher?.let {
                    trySend(
                        try {
                            EncryptionResult.Success(
                                cipherValue = it.encryptData(value)
                            )
                        } catch (ex: Exception){
                            EncryptionResult.CipherException(
                                exception = ex
                            )
                        }
                    )
                } ?: trySend(EncryptionResult.NotAvailable)
                cancel()
            }

        }
        val prompt = BiometricPrompt(
            activity,
            executor,
            callback
        )
        prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        awaitClose {
            cancel()
        }
    }

    @SuppressLint("NewApi")
    override suspend fun decryption(key: String, value: CiphertextWrapper): Flow<DecryptionResult> = callbackFlow {
        val cipher = CipherDecryption(
            keyName = key,
            initializationVector = value.initializationVector
        )
        val executor = ContextCompat.getMainExecutor(activity.baseContext)
        val callback = object : BiometricPrompt.AuthenticationCallback(){

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if(errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON){
                    trySend(DecryptionResult.Cancel)
                } else {
                    trySend(
                        DecryptionResult.Error(
                            errorCode = errorCode,
                            description = errString.toString()
                        )
                    )
                }
                cancel()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                trySend(DecryptionResult.Failed)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                result.cryptoObject?.cipher?.let {
                    trySend(
                        try {
                            DecryptionResult.Success(
                                value = it.decryptData(value.ciphertext)
                            )
                        } catch (ex: Exception){
                            DecryptionResult.CipherException(
                                exception = ex
                            )
                        }
                    )
                } ?: trySend(DecryptionResult.NotAvailable)
                cancel()
            }

        }
        val prompt = BiometricPrompt(
            activity,
            executor,
            callback
        )
        prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        awaitClose {
            cancel()
        }
    }

}