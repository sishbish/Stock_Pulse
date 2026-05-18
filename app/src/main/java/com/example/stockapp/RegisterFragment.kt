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

class RegisterFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val viewModel: StockViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        auth = FirebaseAuth.getInstance()

        return ComposeView(requireContext()).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#121212"))

            setContent {
                StockPulseTheme {
                    RegisterScreen(
                        onRegisterClick = { email, password ->
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnSuccessListener {
                                    viewModel.restoreFromFirestore()
                                    findNavController().navigate(R.id.action_registerFragment_to_dashboardFragment)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), "Registration failed: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                        },
                        onLoginNavigate = {
                            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                        }
                    )
                }
            }
        }
    }
}