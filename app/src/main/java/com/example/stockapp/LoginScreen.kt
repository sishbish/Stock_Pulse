package com.example.stockapp

// 3RD-PARTY LIBRARIES USED:
// 1. Jetpack Compose (androidx.compose.*) - Handles UI rendering, password masks, and state mutation tracking.
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onRegisterNavigate: () -> Unit
) {
//     Stores the text typed into the email entry box.
    var email by remember { mutableStateOf("") }

//     Stores the text typed into the password entry box.
    var password by remember { mutableStateOf("") }

//     Tracks if an error message warning should show up for empty input entries.
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//         Main welcome title header at the top.
        Text(text = "Stock Pulse Login", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

//         Outlined entry text field designed for email handling.
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; showError = false },
            label = { Text("Email Address") },
//             Switches the digital keyboard layout for email symbol keys.
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

//        entry text field designed for password handling.
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; showError = false },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

//         draws a red warning label if blank input forms are submitted.
        if (showError) {
            Text(
                text = "Fields cannot be blank",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
//                 makes sure neither box is empty before passing data forward.
                if (email.isBlank() || password.isBlank()) {
                    showError = true
                } else {
//                     Sends the trimmed email and password inputs
                    onLoginClick(email.trim(), password.trim())
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign In")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Registration button
        TextButton(onClick = onRegisterNavigate) {
            Text("Register if you don't already have an account")
        }
    }
}