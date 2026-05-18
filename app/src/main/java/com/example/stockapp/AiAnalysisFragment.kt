package com.example.stockapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch

class AiAnalysisFragment : Fragment() {

    private val viewModel: StockViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ticker = arguments?.getString("ticker") ?: ""

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    val watchlist by viewModel.watchlist.observeAsState(initial = emptyList())
                    val stock = watchlist.find { it.ticker.equals(ticker, ignoreCase = true) }

                    if (stock != null) {
                        AiAnalysisScreen(
                            stock = stock,
                            onBackClick = { findNavController().navigateUp() },
                            onFetchAnalysis = { targetStock, onResult ->
                                // Safe async coroutine processing on background threads
                                viewLifecycleOwner.lifecycleScope.launch {
                                    try {
                                        // FIX: Invoke fetchAiAnalysis with the explicit parameter mapping your client expects
                                        val analysisText = OpenRouterClient.fetchAiAnalysis(
                                            ticker = targetStock.ticker,
                                            companyName = targetStock.companyName,
                                            lastPrice = targetStock.lastPrice,
                                            changePercent = targetStock.changePercent
                                        )
                                        onResult(Result.success(analysisText))
                                    } catch (e: Exception) {
                                        onResult(Result.failure(e))
                                    }
                                }
                            }
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Stock not found in database.")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAnalysisScreen(
    stock: StockEntity,
    onBackClick: () -> Unit,
    onFetchAnalysis: (StockEntity, (Result<String>) -> Unit) -> Unit
) {
    var aiSummary by remember { mutableStateOf("Click the button below to generate a market report.") }
    var aiVerdict by remember { mutableStateOf("PENDING") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${stock.ticker} AI Insights") },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stock.companyName,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Market Recommendation", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))

                    // Conditionally tint our financial recommendation text labels
                    val verdictColor = when (aiVerdict) {
                        "BUY" -> Color(0xFF388E3C)
                        "SELL" -> Color.Red
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(text = aiVerdict, style = MaterialTheme.typography.headlineSmall, color = verdictColor)
                }
            }

            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Analysis Summary", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator() // Modern replacement for your XML ProgressBar
                        }
                    } else {
                        Text(text = aiSummary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    isLoading = true
                    aiSummary = "Consulting AI market models..."
                    onFetchAnalysis(stock) { result ->
                        isLoading = false
                        result.fold(
                            onSuccess = { rawText ->
                                aiSummary = rawText
                                aiVerdict = when {
                                    rawText.contains("BUY", ignoreCase = true) -> "BUY"
                                    rawText.contains("SELL", ignoreCase = true) -> "SELL"
                                    else -> "HOLD"
                                }
                            },
                            onFailure = { error ->
                                aiSummary = "Error fetching analysis: ${error.localizedMessage}"
                                aiVerdict = "ERROR"
                            }
                        )
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate AI Report")
            }
        }
    }
}