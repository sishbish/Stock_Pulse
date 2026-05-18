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
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val viewModel: StockViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        auth = FirebaseAuth.getInstance()

        // Explicit check: Skip immediately to dashboard if user session is already validated
        if (auth.currentUser != null) {
            findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
        }

        return ComposeView(requireContext()).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#121212"))

            setContent {
                StockPulseTheme {
                    LoginScreen(
                        onLoginClick = { email, password ->
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener {
                                    viewModel.restoreFromFirestore()
                                    findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), "Login failed: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                        },
                        onRegisterNavigate = {
                            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
                        }
                    )
                }
            }
        }
    }
}