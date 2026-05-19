package com.example.stockapp
import androidx.lifecycle.LiveData
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

// 3RD-PARTY LIBRARIES USED:
// 1. Google Firebase Auth (com.google.firebase.auth.auth) - Used for user registration and session management.
// 2. Google Firebase Firestore (com.google.firebase.firestore.firestore) - Used for cloud watch-list backup syncing.
// 3. Kotlinx Coroutines Play Services (kotlinx.coroutines.tasks.await) - Used to bridge Firebase Tasks with Kotlin Suspend Coroutines.

class StockRepository(private val dao: StockDao) {

    val watchlist: LiveData<List<StockEntity>> = dao.getAllStocks()

    // Calls the Alpha Vantage API to get a quote, falls back onto daily metrics if closed,
    // then creates a StockEntity object saved to the room db
    suspend fun addStock(ticker: String): StockEntity? {
        try {
            val response = RetrofitClient.api.getQuote(symbol = ticker)
            val quote = response.globalQuote

            if (quote.symbol.isNullOrEmpty() || quote.price.isNullOrEmpty()) {
                android.util.Log.e("StockRepository", "Invalid ticker or API limit reached")
                return null
            }

            //Safe parsing from the Global Quote response first
            var openPrice = quote.open.toDoubleOrNull() ?: 0.0
            var highPrice = quote.high.toDoubleOrNull() ?: 0.0
            var lowPrice = quote.low.toDoubleOrNull() ?: 0.0
            var calculatedChangePercent = quote.changePercent

            //If values are 0.0, the market likely isn't open yet today or its a weekend.
            // Fetch the historical daily endpoint to extract statistics from the last valid session.
            if (openPrice == 0.0 || highPrice == 0.0 || lowPrice == 0.0) {
                try {
                    val dailyResponse = RetrofitClient.api.getDaily(symbol = ticker)

                    // Sort and pull the last active session's entry
                    val latestEntry = dailyResponse.timeSeries?.entries?.sortedBy { it.key }?.lastOrNull()

                    if (latestEntry != null) {
                        val dailyData = latestEntry.value
                        openPrice = dailyData.open.toDoubleOrNull() ?: openPrice
                        highPrice = dailyData.high.toDoubleOrNull() ?: highPrice
                        lowPrice = dailyData.low.toDoubleOrNull() ?: lowPrice

                        // compute the daily fallback change percent if needed
                        val closePrice = dailyData.close.toDoubleOrNull() ?: 0.0
                        if (openPrice > 0.0) {
                            val rawPercentage = ((closePrice - openPrice) / openPrice) * 100
                            calculatedChangePercent = String.format(java.util.Locale.US, "%.2f%%", rawPercentage)
                        }
                        android.util.Log.d("StockRepository", "Fallback successful! Loaded daily info from: ${latestEntry.key}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("StockRepository", "Failed to retrieve historical daily fallback: ${e.message}")
                }
            }

//          puts the data into a StockEntity object
            val entity = StockEntity(
                ticker = quote.symbol,
                companyName = ticker,
                lastPrice = quote.price.toDoubleOrNull() ?: 0.0,
                changePercent = calculatedChangePercent,
                open = openPrice,
                high = highPrice,
                low = lowPrice,
                volume = quote.volume ?: "0"
            )

//            saves object to room db
            dao.insertStock(entity)
            return entity
        } catch (e: Exception) {
            android.util.Log.e("StockRepository", "Error adding stock: ${e.message}", e)
            return null
        }
    }

    // Sync stocks to the users Firestore backup
    fun backupToFirestore(stock: StockEntity) {
        val db = Firebase.firestore
        val userId = Firebase.auth.currentUser?.uid ?: return
        db.collection("users")
            .document(userId)
            .collection("watchlist")
            .document(stock.ticker)
            .set(mapOf(
                "ticker" to stock.ticker,
                "companyName" to stock.companyName,
                "lastPrice" to stock.lastPrice,
                "changePercent" to stock.changePercent,
                "open" to stock.open,
                "high" to stock.high,
                "low" to stock.low,
                "volume" to stock.volume
            ))
    }

    // Delete stock from users firestore backup
    fun deleteFromFirestore(ticker: String) {
        val db = Firebase.firestore
        val userId = Firebase.auth.currentUser?.uid ?: return
        db.collection("users")
            .document(userId)
            .collection("watchlist")
            .document(ticker)
            .delete()
    }

    // Fetches all firestore docs and inserts them into the room db.
    suspend fun restoreFromFirestore() {
        val db = Firebase.firestore
        val userId = Firebase.auth.currentUser?.uid ?: return
        try {
            db.collection("users")
                .document(userId)
                .collection("watchlist")
                .get()
                .await()
                .documents.forEach { doc ->
                    val entity = StockEntity(
                        ticker = doc.getString("ticker") ?: return@forEach,
                        companyName = doc.getString("companyName") ?: "",
                        lastPrice = doc.getDouble("lastPrice") ?: 0.0,
                        changePercent = doc.getString("changePercent") ?: "0.00%",
                        open = doc.getDouble("open") ?: 0.0,
                        high = doc.getDouble("high") ?: 0.0,
                        low = doc.getDouble("low") ?: 0.0,
                        volume = doc.getString("volume") ?: "0"
                    )
                    dao.insertStock(entity)
                }
        } catch (e: Exception) {
            android.util.Log.e("StockRepository", "Error restoring backup: ${e.message}")
        }
    }

    // Calls the daily time series endpoint from the API and separates it into distinct periods.
    // Returns a list of (date, price) pairs so the chart can display labelled axes.
    suspend fun getChartData(ticker: String, period: String): List<Pair<String, Float>> {
        return try {
            val response = RetrofitClient.api.getDaily(symbol = ticker)
            val entries = response.timeSeries?.entries
                ?.sortedBy { it.key } ?: return emptyList()

            when (period) {
                "1D" -> {
                    return try {
                        delay(1100L)
                        val intradayResponse = RetrofitClient.api.getIntraday(symbol = ticker)
                        val intradayEntries = intradayResponse.timeSeries?.entries
                            ?.sortedBy { it.key }

                        if (!intradayEntries.isNullOrEmpty()) {
                            val rawPoints = intradayEntries.takeLast(78)
                                .map { Pair(it.key, it.value.close.toFloat()) }
                            if (rawPoints.size >= 2) rawPoints else emptyList()
                        } else {
                            android.util.Log.d("StockRepository", "Intraday unavailable, falling back to 1W daily data")
                            delay(1100L)
                            val dailyResponse = RetrofitClient.api.getDaily(symbol = ticker)
                            val dailyEntries = dailyResponse.timeSeries?.entries
                                ?.sortedBy { it.key } ?: return emptyList()
                            val rawPoints = dailyEntries.takeLast(7)
                                .map { Pair(it.key, it.value.close.toFloat()) }
                            if (rawPoints.size >= 2) rawPoints else emptyList()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("StockRepository", "1D chart error: ${e.message}")
                        emptyList()
                    }
                }
                "1M" -> entries.takeLast(30).map { Pair(it.key, it.value.close.toFloat()) }
                "1Y" -> entries.takeLast(252).map { Pair(it.key, it.value.close.toFloat()) }
                else -> emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("StockRepository", "Chart error: ${e.message}")
            emptyList()
        }
    }

    suspend fun clearLocalData() {
        dao.deleteAllStocks()
    }

    suspend fun deleteStock(stock: StockEntity) {
        dao.deleteStock(stock)
    }

    suspend fun setTargetPrice(ticker: String, targetPrice: Double) {
        dao.setTargetPrice(ticker, targetPrice)
    }
}