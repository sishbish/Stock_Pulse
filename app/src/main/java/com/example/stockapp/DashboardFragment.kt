package com.example.stockapp

// 3RD-PARTY LIBRARIES USED:
// 1. Jetpack Compose (androidx.compose.*) - Used for declarative UI layouts, scroll optimization, and theme definitions.
// 2. Jetpack Compose Runtime LiveData (observeAsState) - Converts Room LiveData models into reactive Compose states.
// 3. Google Firebase Auth (FirebaseAuth) - Handles remote sign-out workflows on logout actions.
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class DashboardFragment : Fragment() {

    private val viewModel: StockViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // ComposeView acts as the platform window layout bridge inside standard fragments
        return ComposeView(requireContext()).apply {
            setContent {
                StockPulseTheme {
                    DashboardScreen(
                        viewModel = viewModel,
                        onStockClick = { stock ->
                            // Safe args configuration mapping to your existing Navigation graph directions
                            val bundle = Bundle().apply { putString("ticker", stock.ticker) }
                            findNavController().navigate(R.id.action_dashboardFragment_to_stockDetailFragment, bundle)
                        },
                        onFabClick = {
                            findNavController().navigate(R.id.action_dashboardFragment_to_addStockFragment)
                        },
                        onLogoutClick = {
                            viewModel.clearLocalData()
                            FirebaseAuth.getInstance().signOut()
                            findNavController().navigate(R.id.action_dashboardFragment_to_loginFragment)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: StockViewModel,
    onStockClick: (StockEntity) -> Unit,
    onFabClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    // Converts your Room DB LiveData collection into a reactive Compose State
    val watchlist by viewModel.watchlist.observeAsState(initial = emptyList())

    // Scaffold provides native structural anchors for TopBars, Content areas, and FAB placement
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock Pulse Watchlist") },
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout App"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onFabClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Tracked Stock")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (watchlist.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No stocks added yet. Tap + to add.", color = Color.Gray)
                }
            } else {
                // High-performance scroll system replacing the traditional RecyclerView
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        items = watchlist,
                        key = { it.ticker } // Key helps Compose keep smooth track of items when changed
                    ) { stock ->

                        // SwipeToDismissBox implements the gesture functionality directly
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.StartToEnd ||
                                    dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteStock(stock)
                                    true
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color by animateColorAsState(
                                    when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.Settled -> Color.Transparent
                                        else -> Color.Red
                                    }, label = "DeleteBackground"
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                                        Text("Delete", color = Color.White, style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            },
                            content = {
                                // Calls your newly written custom stock row card layout
                                StockItemRow(stock = stock, onItemClick = onStockClick)
                            }
                        )
                    }
                }
            }
        }
    }
}