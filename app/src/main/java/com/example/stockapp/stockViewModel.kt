package com.example.stockapp

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


//Extends AndroidViewModel for the Application context for WorkManager
class StockViewModel(application: Application) : AndroidViewModel(application) {

//    creates StockRepository using the application context
    private val repo = StockRepository(AppDatabase.getDatabase(application).stockDao())

//    Accesses the watchlist from the repo
    val watchlist: LiveData<List<StockEntity>> = repo.watchlist

//    Starts coroutine and adds the stock to the repo and then backs it up to Firestore
    fun addStock(ticker: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val entity = repo.addStock(ticker)
            entity?.let { repo.backupToFirestore(it) }
            onComplete()
        }
    }

//    Wipes room db on logout
    fun clearLocalData() {
        viewModelScope.launch {
            repo.clearLocalData()
        }
    }

//    delete stock from room and firestore
    fun deleteStock(stock: StockEntity) {
        viewModelScope.launch {
            repo.deleteStock(stock)
            repo.deleteFromFirestore(stock.ticker)
        }
    }

//    retrieves from firestore after login
    fun restoreFromFirestore() {
        viewModelScope.launch {
            repo.restoreFromFirestore()
        }
    }

//    sets the target pice for the stock alert
    fun setTargetPrice(ticker: String, targetPrice: Double) {
        viewModelScope.launch {
            repo.setTargetPrice(ticker, targetPrice)
        }
    }

//    makes it so PriceAlertWorker runs every 15 mins using WorkManager
//    Keep policy so that a second call does not restart the existing worker
    fun scheduleAlerts(context: Context) {
        val request = PeriodicWorkRequestBuilder<PriceAlertWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "price_alerts",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

//    called from lifecycleScope
    suspend fun getChartData(ticker: String, period: String): List<Pair<String, Float>> {
        return repo.getChartData(ticker, period)
    }
}