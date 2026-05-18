package com.example.stockapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.stockapp.databinding.FragmentAiAnalysisBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

//AI analysis screen. Gives a Bullish or Bearish verdict along with some reasoning
class AiAnalysisFragment : Fragment() {

    private var _binding: FragmentAiAnalysisBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        receives ticker through argument
        val ticker = arguments?.getString("ticker")

//        back navigation in toolbar
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // hide verdict and summary, show loading
        binding.tvVerdict.visibility = View.GONE
        binding.tvSummary.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

//        LLM prompt for the analysis
        lifecycleScope.launch {
            try {
                val prompt = """
            Analyze the stock $ticker and provide:
            1. A one word verdict: either exactly "Bullish" or "Bearish"
            2. Three bullet points explaining your reasoning
            
            Format your response exactly like this:
            VERDICT: Bullish
            • First reason
            • Second reason  
            • Third reason
        """.trimIndent()

//                using OpenRouter LLM API
                val response = OpenRouterClient.api.analyze(
                    request = OpenRouterRequest(
                        messages = listOf(OpenRouterMessage(role = "user", content = prompt))
                    )
                )
                val text = response.choices.first().message.content
                android.util.Log.d("AiAnalysis", "Response: $text")
                val lines = text.trim().split("\n")
                val verdict = lines[0].replace("VERDICT: ", "").trim()
                val summary = lines.drop(1).joinToString("\n")

                binding.progressBar.visibility = View.GONE
                binding.tvVerdict.visibility = View.VISIBLE
                binding.tvSummary.visibility = View.VISIBLE
                binding.tvVerdict.text = verdict
                binding.tvSummary.text = summary
                binding.tvVerdict.setTextColor(
                    if (verdict == "Bullish")
                        android.graphics.Color.parseColor("#1be600")
                    else
                        android.graphics.Color.parseColor("#eb4034")
                )

//                error handling
            } catch (e: Exception) {
                android.util.Log.e("AiAnalysis", "Full error: ${e.javaClass.simpleName} - ${e.message}", e)
                binding.progressBar.visibility = View.GONE
                binding.tvSummary.visibility = View.VISIBLE
                binding.tvSummary.text = when {
                    e.message?.contains("429") == true -> "Rate limit reached. Please wait a minute and try again."
                    e.message?.contains("404") == true -> "Model not found. Please check API configuration."
                    else -> "Failed to load analysis: ${e.message}"
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}