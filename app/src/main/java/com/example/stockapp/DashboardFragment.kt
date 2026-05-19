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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

// Fragment for the main dashboard
class DashboardFragment : Fragment() {

    private val viewModel: StockViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                StockPulseTheme {
                    DashboardScreen(
                        viewModel = viewModel,
                        onStockClick = { stock ->
//                             When a user clicks a stock card, save the ticker and move to the details page
                            val bundle = Bundle().apply { putString("ticker", stock.ticker) }
                            findNavController().navigate(R.id.action_dashboardFragment_to_stockDetailFragment, bundle)
                        },
                        onFabClick = {
//                             Navigates to the add stock page
                            findNavController().navigate(R.id.action_dashboardFragment_to_addStockFragment)
                        },
                        onLogoutClick = {
//                             Clears room db memory data caching, signs out of Firebase online sessions, and routes back to login
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
//     Pulls the current watchlist data from storage and automatically refreshes the UI if things change
    val watchlist by viewModel.watchlist.observeAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Watchlist") },
                actions = {
//                     Placing the logout button icon on the right side of the toolbar header
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
//             The plus button for adding stocks.
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
//             Checking if the user's ticker list database collection is completely empty.
            if (watchlist.isEmpty()) {
//                 notification text displayed if there are no items to list.
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No stocks added yet. Tap + to add.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        items = watchlist,
                        key = { it.ticker }
                    ) { stock ->

//                         SwipeToDismissBox implements the gesture functionality directly
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
//                                 If the row card is swiped all the way left or right, run delete tasks.
                                if (dismissValue == SwipeToDismissBoxValue.StartToEnd ||
                                    dismissValue == SwipeToDismissBoxValue.EndToStart) {
//                                     Deletes the row item from the database.
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
//                                 Handles animating a transition behind the card during user swiping actions.
                                val shiftingColour by animateColorAsState(
                                    when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.Settled -> Color.Transparent
                                        else -> Color.Red
                                    }, label = "DeleteBackground"
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(shiftingColour)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
//                                     Displays a "Delete" label warning when the user pulls card.
                                    if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                                        Text("Delete", color = Color.White, style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            },
                            content = {
//                                 Loads the visual structural card row containing the stock price information.
                                StockItemRow(stock = stock, onItemClick = onStockClick)
                            }
                        )
                    }
                }
            }
        }
    }
}