# **Biometric compose**

![logo][logo]

Biometric compose is a simple library to use biometrics from compose with little code.

## **Download**

```gradle

allprojects {
	repositories {
	    ...
		maven { url 'https://jitpack.io' }
	}
}

dependencies {
	implementation 'com.github.gucodero:biometric-compose:1.0'
}

```

## **Use**

Your activity must extend FragmentActivity or subclass


```kotlin

class MainActivity : FragmentActivity()

```

You can validate if biometrics is available

```kotlin

val isBiometricPromptAvailable: Boolean = isBiometricPromptIsAvailable(
    allowedAuthenticators = AllowedAuthenticators.BIOMETRIC_STRONG
)

```

To create a biometricPrompt we use the composable method rememberBiometricPrompt

```kotlin

val biometricPrompt = rememberBiometricPrompt(
    allowedAuthenticators = AllowedAuthenticators.BIOMETRIC_STRONG,
    title = stringResource(id = R.string.title),
    subtitle = stringResource(id = R.string.subtitle),
    description = stringResource(id = R.string.description),
    negativeButtonText = stringResource(id = R.string.negative_button),
    confirmationRequired = false
)

```

For encryption run the encryption function inside a coroutine

```kotlin

biometricPrompt.encryption(
    key = "MY_KEY",
    value = "MY_TOKEN"
).collect {
    when(it){
        is EncryptionResult.Success -> {
            val cipherValue = it.cipherValue
        }
        is EncryptionResult.Failed -> {}
        is EncryptionResult.Error -> {
            val errorCode = it.errorCode
            val description = it.description
        }
        is EncryptionResult.Cancel -> {}
        is EncryptionResult.NotAvailable -> {}
        is EncryptionResult.CipherException -> {
            val exception = it.exception
        }
    }
}

```

For decryption run the encryption function inside a coroutine

```kotlin

//This object must be saved to local storage for later retrieval at decryption time
val value: CiphertextWrapper = myLocalStorage()

biometricPrompt.decryption(
    key = "MY_KEY",
    value = value
).collect {
    when(it){
        is DecryptionResult.Success -> {
            val cipherValue = it.cipherValue
        }
        is DecryptionResult.Failed -> {}
        is DecryptionResult.Error -> {
            val errorCode = it.errorCode
            val description = it.description
        }
        is DecryptionResult.Cancel -> {}
        is DecryptionResult.NotAvailable -> {}
        is DecryptionResult.CipherException -> {
            val exception = it.exception
        }
    }
}

```

## LICENCIA

```
MIT License
Copyright (c) 2022 Gucodero
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

[logo]: https://github.com/gucodero/biometric-compose/blob/main/doc/img/logo.jpeg