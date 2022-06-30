package com.gucodero.biometric_compose.entities

sealed class EncryptionResult {
    class Success(
        val cipherValue: CiphertextWrapper
    ): EncryptionResult()
    class Error(
        val errorCode: Int,
        val description: String
    ): EncryptionResult()
    object Cancel: EncryptionResult()
    object Failed: EncryptionResult()
    object NotAvailable: EncryptionResult()
    class CipherException(
        val exception: Exception
    ): EncryptionResult()
}