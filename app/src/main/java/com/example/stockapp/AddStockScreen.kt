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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStockScreen(
    onBackClick: () -> Unit,
    onAddStockClick: (String) -> Unit
) {
    var tickerInput by remember { mutableStateOf("") }
    var isInputError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Ticker Symbol") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Enter the stock ticker symbol you wish to track (e.g., AAPL, TSLA, MSFT).",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = tickerInput,
                onValueChange = {
                    tickerInput = it
                    if (it.isNotEmpty()) isInputError = false
                },
                label = { Text("Ticker Symbol") },
                placeholder = { Text("e.g. AAPL") },
                singleLine = true,
                isError = isInputError,
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (isInputError) {
                        Text("Ticker symbol cannot be blank", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (tickerInput.trim().isEmpty()) {
                        isInputError = true
                    } else {
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