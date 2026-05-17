package com.example.stockapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stockapp.databinding.FragmentDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

//main screen. Sets up recycler view with StockAdapter
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockViewModel by viewModels()

    private lateinit var adapter: StockAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        clicking a stock navigates to StockDetailFragment with ticker as an argument
        adapter = StockAdapter { stock ->
            val bundle = Bundle().apply { putString("ticker", stock.ticker) }
            findNavController().navigate(R.id.action_dashboardFragment_to_stockDetailFragment, bundle)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // set up FAB click listener here
        binding.fab.setOnClickListener {
            // navigate to AddStockFragment
            findNavController().navigate(R.id.action_dashboardFragment_to_addStockFragment)
        }

//        Implements swipe to delete when a stock is swiped
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

//            calls the deleteStock method when swiped
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val stock = adapter.stocks[viewHolder.adapterPosition]
                viewModel.deleteStock(stock)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

//        toolbar has a logout that clears local data, signs out of firebase and navigates back to login
        binding.toolbar.inflateMenu(R.menu.dashboard_menu)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    viewModel.clearLocalData()
                    FirebaseAuth.getInstance().signOut()
                    findNavController().navigate(R.id.action_dashboardFragment_to_loginFragment)
                }
            }
            true
        }

//        observes watchlist to keep list up to date
        viewModel.watchlist.observe(viewLifecycleOwner) { stocks ->
            adapter.stocks = stocks
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}