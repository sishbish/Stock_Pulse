package com.example.stockapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController

//Add stock page. Takes the inputted stock and adds it to the page
class AddStockFragment : Fragment() {

    private val viewModel: StockViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Sets the background of the screen window to black
            setBackgroundColor(android.graphics.Color.parseColor("#121212"))

            setContent {
                // Applies the custom visual styling rules
                StockPulseTheme {
                    AddStockScreen(
                        onBackClick = {
//                            Backwards navigation
                            findNavController().navigateUp()
                        },
                        onAddStockClick = { inputTicker ->
                            // Takes the ticker text typed by the user and searches for it using the app storage logic
                            viewModel.addStock(inputTicker) {
                                requireActivity().runOnUiThread {
                                    // Pops up a toast at the bottom of the screen to tell the user the stock was processed.
                                    Toast.makeText(requireContext(), "$inputTicker processing complete", Toast.LENGTH_SHORT).show()
                                    // Automatically closes the add page and goes back to the previous dashboard.
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