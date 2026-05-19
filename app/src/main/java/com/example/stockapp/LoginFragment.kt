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

//Fragment for the login page
class LoginFragment : Fragment() {

//     Creates the connection to Firebase
    private lateinit var auth: FirebaseAuth

    private val viewModel: StockViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//         Initialises the Firebase account system verification tools.
        auth = FirebaseAuth.getInstance()

//         If the user has already signed in previously and their session is still active,
//         skip the login and go into the main dashboard list.
        if (auth.currentUser != null) {
            findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
        }

        return ComposeView(requireContext()).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#121212"))

            setContent {
                StockPulseTheme {
                    LoginScreen(
                        onLoginClick = { email, password ->
//                             Submits the typed email and password strings to the online server.
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener {
//                                     If the password matches, download and restore their saved watchlist backup rows from Firestore.
                                    viewModel.restoreFromFirestore()
                                    findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
                                }
                                .addOnFailureListener {
//                                     If something goes wrong create a toast showing the error message text.
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