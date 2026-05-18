package com.example.stockapp

// 3RD-PARTY LIBRARIES USED:
// 1. Jetpack Compose UI (ComposeView) - Used to anchor your composable screen layout tree inside traditional fragments.
// 2. Jetpack Compose Runtime LiveData (observeAsState) - Reactively maps specific entity data rows out of the database room.
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController

class StockDetailFragment : Fragment() {

    private val viewModel: StockViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Extracting target ticker passed through navigation bundles
        val ticker = arguments?.getString("ticker") ?: ""

        return ComposeView(requireContext()).apply {
            setContent {
                StockPulseTheme {
                    val watchlist by viewModel.watchlist.observeAsState(initial = emptyList())
                    val stockEntity = watchlist.find { it.ticker.equals(ticker, ignoreCase = true) }

                    if (stockEntity != null) {
                        StockDetailScreen(
                            stock = stockEntity,
                            onBackClick = {
                                findNavController().navigateUp()
                            },
                            onExternalViewClick = {
                                // Implicit Intent to open external web app as requested by mandatory requirements
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://finance.yahoo.com/quote/${stockEntity.ticker}"))
                                startActivity(intent)
                            },
                            onAiAnalysisClick = {
                                // Handle navigation passing the ticker onto your AI Commentary layout sub-screen
                                val bundle = Bundle().apply { putString("ticker", stockEntity.ticker) }
                                findNavController().navigate(R.id.action_stockDetailFragment_to_aiAnalysisFragment, bundle)
                            }
                        )
                    } else {
                        // Safe rendering feedback container if entity loading state is interrupted
                        Text(text = "Loading Stock Data...")
                    }
                }
            }
        }
    }
}