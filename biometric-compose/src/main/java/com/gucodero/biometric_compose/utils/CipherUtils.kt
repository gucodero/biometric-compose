package com.gucodero.biometric_compose.utils

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import com.gucodero.biometric_compose.entities.CiphertextWrapper
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val KEY_SIZE = 256
private const val ANDROID_KEY_STORE = "AndroidKeyStore"

@RequiresApi(Build.VERSION_CODES.M)
private fun createCipher(): Cipher {
    val cipherInfo = listOf(
        KeyProperties.KEY_ALGORITHM_AES,
        KeyProperties.BLOCK_MODE_GCM,
        KeyProperties.ENCRYPTION_PADDING_NONE
    )
    return Cipher.getInstance(cipherInfo.joinToString("/"))
}

@RequiresApi(Build.VERSION_CODES.M)
private fun createSecretKey(keyName: String): SecretKey {
    KeyStore.getInstance(ANDROID_KEY_STORE).apply {
        load(null)
        getKey(keyName, null)?.let { return it as SecretKey }
    }
    val paramsBuilder = KeyGenParameterSpec.Builder(
        keyName,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    ).apply {
        setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        setKeySize(KEY_SIZE)
        setUserAuthenticationRequired(true)
    }
    val keyGenParams = paramsBuilder.build()
    val keyGenerator = KeyGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_AES,
        ANDROID_KEY_STORE
    )
    keyGenerator.init(keyGenParams)
    return keyGenerator.generateKey()
}

@RequiresApi(Build.VERSION_CODES.M)
object CipherEncryption {
    operator fun invoke(keyName: String): Cipher {
        return createCipher().apply {
            init(
                Cipher.ENCRYPT_MODE,
                createSecretKey(keyName)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.M)
object CipherDecryption {
    operator fun invoke(keyName: String, initializationVector: ByteArray): Cipher {
        return createCipher().apply {
            init(
                Cipher.DECRYPT_MODE,
                createSecretKey(keyName),
                GCMParameterSpec(128, initializationVector)
            )
        }
    }
}

fun Cipher.encryptData(plainText: String): CiphertextWrapper {
    val ciphertext = doFinal(plainText.toByteArray(Charset.forName("UTF-8")))
    return CiphertextWrapper(
        ciphertext = ciphertext,
        initializationVector = iv
    )
}

fun Cipher.decryptData(ciphertext: ByteArray): String {
    val plainText = doFinal(ciphertext)
    return String(plainText, Charset.forName("UTF-8"))
}