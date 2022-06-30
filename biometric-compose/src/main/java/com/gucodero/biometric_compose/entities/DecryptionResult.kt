package com.gucodero.biometric_compose.entities

sealed class DecryptionResult {
    class Success(
        val value: String
    ): DecryptionResult()
    class Error(
        val errorCode: Int,
        val description: String
    ): DecryptionResult()
    object Cancel: DecryptionResult()
    object Failed: DecryptionResult()
    object NotAvailable: DecryptionResult()
    class CipherException(
        val exception: Exception
    ): DecryptionResult()
}