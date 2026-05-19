package com.example.stockapp

import android.content.Intent
import android.net.Uri
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
//        gets the key parameter string passed when the user clicks a stock
        val ticker = arguments?.getString("ticker") ?: ""

        return ComposeView(requireContext()).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#121212"))

            setContent {
//                applies the custom styling
                StockPulseTheme {
//                    Pulls the watchlist data and updates the data in real time
                    val watchlist by viewModel.watchlist.observeAsState(initial = emptyList())
                    val stockEntity = watchlist.find { it.ticker.equals(ticker, ignoreCase = true) }

                    if (stockEntity != null) {
                        StockDetailScreen(
                            stock = stockEntity,
                            onFetchChartData = { period ->
                                viewModel.getChartData(stockEntity.ticker, period)
                            },
//                            backward navigatioin
                            onBackClick = { findNavController().navigateUp() },

                            // launches an outside browser app using an Intent to display a financial news search matching the requirement
                            onExternalViewClick = {
                                val queryUrl = "https://uk.finance.yahoo.com/quote/${stockEntity.ticker}/"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(queryUrl))
                                context.startActivity(intent)
                            },

//                            uses the navigation component to safely transition over to the AI Analysis screen fragment
                            onAiAnalysisClick = {
                                val bundle = Bundle().apply { putString("ticker", stockEntity.ticker) }
                                findNavController().navigate(R.id.action_stockDetailFragment_to_aiAnalysisFragment, bundle)
                            },

//                             implements the Android ShareSheet using an implicit intent to let users export text statistics
                            onShareClick = {
                                val shareText = "Checking out ${stockEntity.companyName} (${stockEntity.ticker.uppercase()}) on Stock Pulse. Current Price: $${stockEntity.lastPrice}"
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            },

                            onConfirmPriceAlert = { targetAlertPrice ->
//                                saves price alert to room db
                                viewModel.setTargetPrice(stockEntity.ticker, targetAlertPrice)

//                               shows toast to confirm user alert was saved
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