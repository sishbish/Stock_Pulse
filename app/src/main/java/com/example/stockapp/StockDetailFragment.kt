package com.example.stockapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.WorkManager
import com.example.stockapp.databinding.FragmentStockDetailBinding
import kotlinx.coroutines.launch

// a more detailed page on individual stocks
class StockDetailFragment : Fragment() {

    private var _binding: FragmentStockDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStockDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

//    loads a price graph for the chosen timespan (1 day, 1 month, 1 year)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ticker = arguments?.getString("ticker")

        ticker?.let { loadChart(it, "1D") }

//    timespan selection
        binding.btn1D.setOnClickListener {
            ticker?.let { loadChart(it, "1D") }
        }
        binding.btn1M.setOnClickListener {
            ticker?.let { loadChart(it, "1M") }
        }
        binding.btn1Y.setOnClickListener {
            ticker?.let { loadChart(it, "1Y") }
        }

//    observes the watchlist LiveData to display metrics
        viewModel.watchlist.observe(viewLifecycleOwner) { stocks ->
            val stock = stocks.find { it.ticker == ticker }
            stock?.let {
                binding.tvCompanyName.text = it.companyName
                binding.tvTicker.text = it.ticker
                binding.tvPrice.text = "$${it.lastPrice}"
                binding.tvChangePercent.text = it.changePercent
                binding.tvOpenValue.text = "$${it.open}"
                binding.tvHighValue.text = "$${it.high}"
                binding.tvLowValue.text = "$${it.low}"
                binding.tvVolumeValue.text = it.volume

                // price range bar
                val low = it.low
                val high = it.high
                val currentPrice = it.lastPrice

                val progress = if (high - low > 0) {
                    (((currentPrice - low) / (high - low)) * 100).toInt()
                } else {
                    // Default progress to 0 if data isn't fully loaded or identical
                    0
                }
                binding.priceRangeBar.progress = progress
                binding.tvRangeLow.text = "$${"%.2f".format(low)}"
                binding.tvRangeHigh.text = "$${"%.2f".format(high)}"
            }
        }

//    back navigation on toolbar
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

//    intent to take user to yahoo finance page for current stock
        binding.btnReadNews.setOnClickListener {
            val url = "https://finance.yahoo.com/quote/${ticker}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

//    navigation to AI analysis page
        binding.btnAiAnalysis.setOnClickListener {
            val bundle = Bundle().apply { putString("ticker", ticker) }
            findNavController().navigate(R.id.action_stockDetailFragment_to_aiAnalysisFragment, bundle)
        }

//    Share function that allows user to share the current price
        binding.toolbar.inflateMenu(R.menu.stock_detail_menu)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_share -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "$ticker is currently trading at $${binding.tvPrice.text} via StockPulse")
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share stock quote"))
                }
            }
            true
        }

//    Set alert button reads target price input and schedules the WorkManager alerts
        binding.btnSetAlert.setOnClickListener {
            val targetPriceText = binding.etTargetPrice.text.toString().trim()
            if (targetPriceText.isNotEmpty()) {
                val targetPrice = targetPriceText.toDoubleOrNull()
                if (targetPrice != null && ticker != null) {
                    viewModel.setTargetPrice(ticker, targetPrice)
                    viewModel.scheduleAlerts(requireContext())
                    // Fire worker immediately for testing
                    WorkManager.getInstance(requireContext())
                        .enqueue(androidx.work.OneTimeWorkRequestBuilder<PriceAlertWorker>().build())
                    Toast.makeText(requireContext(), "Alert set for $ticker at $$targetPrice", Toast.LENGTH_SHORT).show()
                    binding.etTargetPrice.text.clear()
                }
            }
        }

    }

//    Dummy data for testing
//    private fun loadChart(ticker: String, period: String) {
//        lifecycleScope.launch {
//            // Temporary mock data - remove when API limit resets
//            val prices = listOf(210f, 208f, 212f, 215f, 213f, 216f, 219f, 218f, 221f, 220f, 223f, 225f)
//
//            val entries = prices.mapIndexed { index, price ->
//                com.github.mikephil.charting.data.Entry(index.toFloat(), price)
//            }

//    Price graph
    private fun loadChart(ticker: String, period: String) {
        lifecycleScope.launch {
            val prices = viewModel.getChartData(ticker, period)
            if (prices.isEmpty()) return@launch

            val entries = prices.mapIndexed { index, price ->
                com.github.mikephil.charting.data.Entry(index.toFloat(), price)
            }

            val dataSet = com.github.mikephil.charting.data.LineDataSet(entries, "").apply {
                color = android.graphics.Color.parseColor("#4DC9FF")
                setDrawCircles(false)
                setDrawValues(false)
                lineWidth = 2f
                mode = com.github.mikephil.charting.data.LineDataSet.Mode.CUBIC_BEZIER
            }

            binding.lineChart.apply {
                data = com.github.mikephil.charting.data.LineData(dataSet)
                description.isEnabled = false
                legend.isEnabled = false
                xAxis.isEnabled = false
                axisRight.isEnabled = false
                axisLeft.textColor = android.graphics.Color.WHITE
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                invalidate()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}