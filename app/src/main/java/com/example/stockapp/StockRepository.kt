package com.example.stockapp
import androidx.lifecycle.LiveData
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class StockRepository(private val dao: StockDao) {

    val watchlist: LiveData<List<StockEntity>> = dao.getAllStocks()

//    calls the Alpha Vantage API to get a quote then creates a StockEntity object which is saved to the
//    room db and returns it
    suspend fun addStock(ticker: String): StockEntity? {
        try {
            val response = RetrofitClient.api.getQuote(symbol = ticker)
            val quote = response.globalQuote

            if (quote.symbol.isNullOrEmpty() || quote.price.isNullOrEmpty()) {
                android.util.Log.e("StockRepository", "Invalid ticker or API limit reached")
                return null
            }

            val entity = StockEntity(
                ticker = quote.symbol,
                companyName = ticker,
                lastPrice = quote.price.toDouble(),
                changePercent = quote.changePercent ?: "0.00%"
            )
            dao.insertStock(entity)
            return entity
        } catch (e: Exception) {
            android.util.Log.e("StockRepository", "Error adding stock: ${e.message}", e)
            return null
        }
    }

//    sync stocks to the users Firestore backup
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
                "changePercent" to stock.changePercent
            ))
    }
//delete stock from users firestore backup
    fun deleteFromFirestore(ticker: String) {
        val db = Firebase.firestore
        val userId = Firebase.auth.currentUser?.uid ?: return
        db.collection("users")
            .document(userId)
            .collection("watchlist")
            .document(ticker)
            .delete()
    }

//    fetches all firestore docs and inserts them into the room db. This happens after a user
//    logs in.
    suspend fun restoreFromFirestore() {
        val db = Firebase.firestore
        val userId = Firebase.auth.currentUser?.uid ?: return
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
                    changePercent = doc.getString("changePercent") ?: "0.00%"
                )
                dao.insertStock(entity)
            }
    }

//calls the daily time series endpoint from the API and then seperates it into 1 day, 1 month,
//1 year periods
    suspend fun getChartData(ticker: String, period: String): List<Float> {
        return try {
            val response = RetrofitClient.api.getDaily(symbol = ticker)
            val entries = response.timeSeries?.entries
                ?.sortedBy { it.key } ?: return emptyList()

            when (period) {
                "1D" -> entries.takeLast(1).map { it.value.close.toFloat() }
                "1M" -> entries.takeLast(30).map { it.value.close.toFloat() }
                "1Y" -> entries.takeLast(252).map { it.value.close.toFloat() }
                else -> emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("StockRepository", "Chart error: ${e.message}")
            emptyList()
        }
    }

//    calls the functions from StockDao
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