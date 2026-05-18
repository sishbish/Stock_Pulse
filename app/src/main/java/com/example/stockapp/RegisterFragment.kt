package com.example.stockapp

// 3RD-PARTY LIBRARIES USED:
// 1. Jetpack Compose UI (ComposeView) - Embeds your registration composable screen inside the fragment frame.
// 2. Google Firebase Auth (FirebaseAuth) - Creates brand new user records inside your firebase console.
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    RegisterScreen(
                        onRegisterClick = { email, password ->
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(requireContext(), "Account created successfully!", Toast.LENGTH_SHORT).show()
                                        findNavController().navigate(R.id.action_registerFragment_to_dashboardFragment)
                                    } else {
                                        Toast.makeText(requireContext(), "Registration Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                        },
                        onLoginNavigate = {
                            findNavController().navigateUp()
                        }
                    )
                }
            }
        }
    }
}