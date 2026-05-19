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

//Fragment for the registration page
class RegisterFragment : Fragment() {

//     Creates the connection to Firebase
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
//                             Submits the newly typed email and password strings to the online secure server to create an account.
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnSuccessListener {
//                                     If account creation is successful, attempt to download and restore any existing data backups from the cloud.
                                    viewModel.restoreFromFirestore()
                                    findNavController().navigate(R.id.action_registerFragment_to_dashboardFragment)
                                }
                                .addOnFailureListener {
//                                     If something goes wrong then create toast showing the error text.
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