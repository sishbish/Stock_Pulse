package com.example.stockapp

// 3RD-PARTY LIBRARIES USED:
// 1. Jetpack Compose (androidx.compose.*) - Handles functional layout structure and input validation fields.
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp


@Composable
fun RegisterScreen(
    onRegisterClick: (String, String) -> Unit,
    onLoginNavigate: () -> Unit
) {
//     Stores the text typed into the email registration input box.
    var email by remember { mutableStateOf("") }

//     Stores the text typed into the password creation input box.
    var password by remember { mutableStateOf("") }

//     Tracks if a warning label should show up due to empty fields or short passwords.
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//         Main sign-up page header title text at the top of the screen.
        Text(text = "Create Account", style = MaterialTheme.typography.headlineLarge, color = Color(0xFFBB86FC))
        Spacer(modifier = Modifier.height(32.dp))

//         Outlined entry text field designed for email input handling.
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; showError = false },
            label = { Text("Email Address") },
            // Switches the digital keyboard layout to prioritize email symbol keys.
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

//         Outlined entry text field designed for password creation input handling.
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; showError = false },
            label = { Text("Password (Min 6 characters)") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

//          Draws a red warning label if the requirements aren't met.
        if (showError) {
            Text(
                text = "Please enter valid email and password rules.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.length < 6) {
                    showError = true
                } else {
                    onRegisterClick(email.trim(), password.trim())
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onLoginNavigate) {
            Text("Already registered? Back to Sign In")
        }
    }
}