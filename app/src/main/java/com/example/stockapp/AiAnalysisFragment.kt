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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch


// Fragment for ai analysis page
class AiAnalysisFragment : Fragment() {

    private val viewModel: StockViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//         Figures out which stock the user clicked on by reading the ticker sent from the previous details screen
        val ticker = arguments?.getString("ticker") ?: ""

        return ComposeView(requireContext()).apply {
            setContent {
//                Applies the styling.
                StockPulseTheme {
                    // Pulls the current watchlist data from storage to keep things synced
                    val watchlist by viewModel.watchlist.observeAsState(initial = emptyList())
                    // Searches through the list of stocks to find the one matching the clicked ticker
                    val stock = watchlist.find { it.ticker.equals(ticker, ignoreCase = true) }

//                     Only builds the screen if we successfully found the stock data in our list
                    if (stock != null) {
                        AiAnalysisScreen(
                            stock = stock,
//                            Backwards navigation
                            onBackClick = { findNavController().navigateUp() },
                            onFetchAnalysis = { targetStock, onResult ->
                                viewLifecycleOwner.lifecycleScope.launch {
                                    try {
//                                        Prompt to be sent to the LLM
                                        val prompt = "Analyze the market status of ${targetStock.companyName} (${targetStock.ticker}) currently trading at $${targetStock.lastPrice} (${targetStock.changePercent}). Provide a brief summary and conclude with a clear line containing either 'BULLISH' or 'BEARISH'."

//                                         Sends the instruction prompt
                                        val analysisText = OpenRouterClient.getAiAnalysis(prompt)

//                                         Delivers the final text paragraph back
                                        onResult(Result.success(analysisText))
                                    } catch (e: Exception) {
//                                         Catches and returns any internet connectivity errors
                                        onResult(Result.failure(e))
                                    }
                                }
                            }
                        )
                    } else {
//                         Fallback message displayed in case the stock can't be loaded from local storage memory
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Stock not found in database.")
                        }
                    }
                }
            }
        }
    }
}

// Design for the ai analysis screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAnalysisScreen(
    stock: StockEntity,
    onBackClick: () -> Unit,
    onFetchAnalysis: (StockEntity, (Result<String>) -> Unit) -> Unit
) {
//     Stores the text summary description returned by the LLM
    var aiSummary by remember { mutableStateOf("Click the button below to generate a market report.") }

//     Tracks the current market stance (BULLISH, BEARISH, or PENDING) to update styles
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

//             Layout Card containing the colour-coded Bullish or Bearish
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Market Recommendation", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))

//                     Automatically swaps the text colour based on the words read inside the final generated report
                    val verdictColour = when (aiVerdict) {
                        "BULLISH" -> Color(0xFF388E3C)
                        "BEARISH" -> Color.Red
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(text = aiVerdict, style = MaterialTheme.typography.headlineSmall, color = verdictColour)
                }
            }

            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Analysis Summary", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))

//                     If the background web call is working, display a turning wheel spinner
//                     Otherwise, display the final paragraphs
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
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
//                         Decodes if the network processing finished successfully or threw an error connection timeout
                        result.fold(
                            onSuccess = { rawText ->
                                aiSummary = rawText
//                                 Scans the sentences inside the paragraph to automatically determine the final verdict banner tag
                                aiVerdict = when {
                                    rawText.contains("BULLISH", ignoreCase = true) -> "BULLISH"
                                    rawText.contains("BEARISH", ignoreCase = true) -> "BEARISH"
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