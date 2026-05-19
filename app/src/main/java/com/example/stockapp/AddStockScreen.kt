package com.example.stockapp

// 3RD-PARTY LIBRARIES USED:
// 1. Jetpack Compose (androidx.compose.*) - Handles UI input rendering, state tracking, and layout arrangement.
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


// Manages the textbox, validation and buttons for typing in a new ticker
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStockScreen(
    onBackClick: () -> Unit,
    onAddStockClick: (String) -> Unit
) {
    // Stores whatever letters the user types into the input box
    var tickerInput by remember { mutableStateOf("") }

    // Tracks if the input box should highlight in red due to an empty entry
    var isInputError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // Adds the top header strip with the screen title
            TopAppBar(
                title = { Text("Add Ticker Symbol") },
                navigationIcon = {
//                    Back navigation
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        // Column stacks all of the input elements vertically down the page
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Instruction text telling the user what they are supposed to type
            Text(
                text = "Enter the stock ticker symbol you wish to track (e.g., AAPL, TSLA, MSFT).",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // The text box where the user types the ticker letters
            OutlinedTextField(
                value = tickerInput,
                onValueChange = {
                    tickerInput = it
                    // Automatically removes the red error state as soon as the user starts typing again
                    if (it.isNotEmpty()) isInputError = false
                },
                label = { Text("Ticker Symbol") },
                placeholder = { Text("e.g. AAPL") },
                singleLine = true,
                isError = isInputError, // Turns the box red if an entry error is found
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    // Displays a red helper warning message under the box if the user leaves it blank
                    if (isInputError) {
                        Text("Ticker symbol cannot be blank", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // The main action button to confirm and add the stock ticker
            Button(
                onClick = {
                    // Checks if the entry is empty or only filled with blank spaces
                    if (tickerInput.trim().isEmpty()) {
                        isInputError = true
                    } else {
                        // Sends the cleaned ticker symbol back to the fragment to be saved
                        onAddStockClick(tickerInput.trim().uppercase())
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add to Watchlist", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}