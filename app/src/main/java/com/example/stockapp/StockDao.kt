package com.example.stockapp

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


//All database operations for the watchlist
@Dao
interface StockDao {

//    Returns all stocks as LiveData, the UI automatically updates when data changes
    @Query("SELECT * FROM watchlist")
    fun getAllStocks(): LiveData<List<StockEntity>>


//    Inserts a stock and replaces a ticker if it already exists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: StockEntity)


//    deletes a stock from the database
    @Delete
    suspend fun deleteStock(stock: StockEntity)

//    wipes entire watchlist when user logs out
    @Query("DELETE FROM watchlist")
    suspend fun deleteAllStocks()


//    Separate stock data retrieval for price alerts. Returns a list of all stocks for the WorkManager
//    to check for price alerts
    @Query("SELECT * FROM watchlist")
    suspend fun getAllStocksSync(): List<StockEntity>

//    updates the target price for a ticker
    @Query("UPDATE watchlist SET targetPrice = :targetPrice WHERE ticker = :ticker")
    suspend fun setTargetPrice(ticker: String, targetPrice: Double)
}