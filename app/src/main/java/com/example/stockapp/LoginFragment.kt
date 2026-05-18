package com.example.stockapp

// 3RD-PARTY LIBRARIES USED:
// 1. Jetpack Compose UI (ComposeView) - Bridges the composable layout tree inside your legacy fragment.
// 2. Google Firebase Auth (FirebaseAuth) - Verifies user credentials on the cloud.
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

class LoginFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Safe check: If the user session is already valid, skip the login screen entirely
        if (auth.currentUser != null) {
            findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
        }

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    LoginScreen(
                        onLoginClick = { email, password ->
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
                                    } else {
                                        Toast.makeText(requireContext(), "Auth Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
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