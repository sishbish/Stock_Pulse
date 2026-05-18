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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch

class AddStockFragment : Fragment() {

    private val viewModel: StockViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    AddStockScreen(
                        onBackClick = {
                            findNavController().navigateUp()
                        },
                        onAddStockClick = { inputTicker ->
                            // Launches the network task and supplies an empty callback block to satisfy the onComplete signature
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