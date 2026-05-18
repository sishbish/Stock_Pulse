package com.example.stockapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController

class StockDetailFragment : Fragment() {

    private val viewModel: StockViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ticker = arguments?.getString("ticker") ?: ""

        return ComposeView(requireContext()).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#121212"))

            setContent {
                StockPulseTheme {
                    val watchlist by viewModel.watchlist.observeAsState(initial = emptyList())
                    val stockEntity = watchlist.find { it.ticker.equals(ticker, ignoreCase = true) }

                    if (stockEntity != null) {
                        StockDetailScreen(
                            stock = stockEntity,
                            onBackClick = { findNavController().navigateUp() },
                            onExternalViewClick = { /* ... */ },
                            onAiAnalysisClick = { /* ... */ },
                            onShareClick = { /* ... */ },
                            onConfirmPriceAlert = { targetAlertPrice ->
                                // Executes Room update asynchronously on your background coroutine IO thread context
                                viewModel.setTargetPrice(stockEntity.ticker, targetAlertPrice)

                                android.widget.Toast.makeText(
                                    requireContext(),
                                    "Alert set for ${stockEntity.ticker.uppercase()} at $${String.format("%.2f", targetAlertPrice)}",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            }
        }
    }
}