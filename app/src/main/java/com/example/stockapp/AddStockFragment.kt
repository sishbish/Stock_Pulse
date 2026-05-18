package com.example.stockapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController

class AddStockFragment : Fragment() {

    private val viewModel: StockViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Force legacy container to match your black theme background layout color
            setBackgroundColor(android.graphics.Color.parseColor("#121212"))

            setContent {
                StockPulseTheme {
                    AddStockScreen(
                        onBackClick = {
                            findNavController().navigateUp()
                        },
                        onAddStockClick = { inputTicker ->
                            // Calls the exact addStock signature with trailing lambda block from your branch
                            viewModel.addStock(inputTicker) {
                                requireActivity().runOnUiThread {
                                    Toast.makeText(requireContext(), "$inputTicker processing complete", Toast.LENGTH_SHORT).show()
                                    findNavController().navigateUp()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}