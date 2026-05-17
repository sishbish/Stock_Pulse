package com.example.stockapp

import androidx.room.Entity
import androidx.room.PrimaryKey

//Class for stock data used in the Room database

//Contains stock ticker, company name, most recent price from the API,
// timestamp from the last API request, daily percentage change,
// price alert set by user (null if nothing set)
@Entity(tableName = "watchlist")
data class StockEntity(
    @PrimaryKey val ticker: String,
    val companyName: String,
    val lastPrice: Double,
    val lastUpdated: Long = System.currentTimeMillis(),
    val changePercent: String = "0.00%",
    val targetPrice: Double? = null
)





