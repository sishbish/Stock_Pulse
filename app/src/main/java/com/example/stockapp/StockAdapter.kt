package com.example.stockapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StockAdapter(private val onClick: (StockEntity) -> Unit) : RecyclerView.Adapter<StockAdapter.ViewHolder>() {
    var stocks: List<StockEntity> = emptyList()
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTicker = view.findViewById<TextView>(R.id.tvTicker)
        val tvCompanyName = view.findViewById<TextView>(R.id.tvCompanyName)
        val tvPrice = view.findViewById<TextView>(R.id.tvPrice)
        val tvChangePercent = view.findViewById<TextView>(R.id.tvChangePercent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stock, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvTicker.text = stocks[position].ticker
        holder.tvCompanyName.text = stocks[position].companyName
        holder.tvPrice.text = "$${stocks[position].lastPrice}"
        val change = stocks[position].changePercent
        holder.tvChangePercent.text = change
        holder.tvChangePercent.setTextColor(
            if (change.startsWith("-"))
                android.graphics.Color.parseColor("#FF4444")  // red
            else
                android.graphics.Color.parseColor("#00C853")  // green
        )
        holder.itemView.setOnClickListener { onClick(stocks[position]) }
    }

    override fun getItemCount(): Int {
        return stocks.size
    }
}