package com.gemwallet.android.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SetCalculatorCodeDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val store = remember { CalculatorCodeStore(context) }
    var code1 by remember { mutableStateOf("") }
    var code2 by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (store.hasCode()) "Change Calculator Code" else "Set Calculator Code") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter a 4 to 10 digit numeric code. When entered into the calculator and '=' is pressed, it will unlock the wallet.")
                OutlinedTextField(
                    value = code1,
                    onValueChange = { code1 = it },
                    label = { Text("Enter Code (4-10 digits)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = code2,
                    onValueChange = { code2 = it },
                    label = { Text("Confirm Code") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (!store.isValidCode(code1)) {
                    errorMessage = "Code must be 4 to 10 digits."
                } else if (code1 != code2) {
                    errorMessage = "Codes do not match."
                } else {
                    store.setCode(code1)
                    onSuccess()
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
