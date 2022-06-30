package com.gucodero.biometric_compose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.gucodero.biometric_compose.entities.AllowedAuthenticators
import com.gucodero.biometric_compose.entities.CiphertextWrapper
import com.gucodero.biometric_compose.entities.DecryptionResult
import com.gucodero.biometric_compose.entities.EncryptionResult
import com.gucodero.biometric_compose.utils.isBiometricPromptIsAvailable
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Screen(
                toast = ::toast
            )
        }
    }

    private fun toast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

}

@Composable
fun Screen(
    toast: (String) -> Unit
){
    val isBiometricPromptAvailable = isBiometricPromptIsAvailable(
        allowedAuthenticators = AllowedAuthenticators.BIOMETRIC_STRONG
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colors.background
            )
            .padding(
                16.dp
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(!isBiometricPromptAvailable){
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.biometric_not_available)
                )
            }
        } else {
            val biometricPrompt = rememberBiometricPrompt(
                allowedAuthenticators = AllowedAuthenticators.BIOMETRIC_STRONG,
                title = stringResource(id = R.string.biometric_prompt_title),
                subtitle = stringResource(id = R.string.biometric_prompt_subtitle),
                description = stringResource(id = R.string.biometric_prompt_description),
                negativeButtonText = stringResource(id = R.string.biometric_prompt_negative_button),
                confirmationRequired = false
            )
            val coroutine = rememberCoroutineScope()
            var key by remember {
                mutableStateOf("")
            }
            var value by remember {
                mutableStateOf("")
            }
            var cipherValue: CiphertextWrapper? by remember {
                mutableStateOf(null)
            }
            if(cipherValue == null){
                TextField(
                    value = key,
                    onValueChange = {
                        key = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "KEY")
                    }
                )
                TextField(
                    value = value,
                    onValueChange = {
                        value = it
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    label = {
                        Text(text = "VALUE")
                    }
                )
                Button(
                    onClick = {
                        coroutine.launch {
                            biometricPrompt.encryption(
                                key = key,
                                value = value
                            ).collect {
                                when(it){
                                    is EncryptionResult.Success -> {
                                        toast("SUCCESS")
                                        cipherValue = it.cipherValue
                                    }
                                    is EncryptionResult.Failed -> {
                                        toast("FAILED")
                                    }
                                    is EncryptionResult.Error -> {
                                        toast("ERROR: ${it.errorCode} - ${it.description}")
                                    }
                                    is EncryptionResult.Cancel -> {
                                        toast("CANCEL")
                                    }
                                    is EncryptionResult.NotAvailable -> {
                                        toast("NotAvailable")
                                    }
                                    is EncryptionResult.CipherException -> {
                                        toast(it.exception.toString())
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    enabled = key.isNotEmpty() && value.isNotEmpty()
                ) {
                    Text(text = "Encryption")
                }
            } else {
                TextField(
                    value = key,
                    onValueChange = {
                        key = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "KEY")
                    }
                )
                Button(
                    onClick = {
                        coroutine.launch {
                            biometricPrompt.decryption(
                                key = key,
                                value = cipherValue!!
                            ).collect {
                                when(it){
                                    is DecryptionResult.Success -> {
                                        toast("SUCCESS: ${it.value}")
                                    }
                                    is DecryptionResult.Failed -> {
                                        toast("FAILED")
                                    }
                                    is DecryptionResult.Error -> {
                                        toast("ERROR: ${it.errorCode} - ${it.description}")
                                    }
                                    is DecryptionResult.Cancel -> {
                                        toast("CANCEL")
                                    }
                                    is DecryptionResult.NotAvailable -> {
                                        toast("NotAvailable")
                                    }
                                    is DecryptionResult.CipherException -> {
                                        toast(it.exception.toString())
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    enabled = key.isNotEmpty()
                ) {
                    Text(text = "Decryption")
                }
                Button(
                    onClick = {
                        cipherValue = null
                        key = ""
                        value = ""
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
                ) {
                    Text(text = "Reset")
                }
            }
        }
    }
}