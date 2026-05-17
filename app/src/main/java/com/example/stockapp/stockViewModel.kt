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


class StockViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = StockRepository(AppDatabase.getDatabase(application).stockDao())

    val watchlist: LiveData<List<StockEntity>> = repo.watchlist

    fun addStock(ticker: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val entity = repo.addStock(ticker)
            entity?.let { repo.backupToFirestore(it) }
            onComplete()
        }
    }

    fun clearLocalData() {
        viewModelScope.launch {
            repo.clearLocalData()
        }
    }

    fun deleteStock(stock: StockEntity) {
        viewModelScope.launch {
            repo.deleteStock(stock)
            repo.deleteFromFirestore(stock.ticker)
        }
    }

    fun restoreFromFirestore() {
        viewModelScope.launch {
            repo.restoreFromFirestore()
        }
    }

    fun setTargetPrice(ticker: String, targetPrice: Double) {
        viewModelScope.launch {
            repo.setTargetPrice(ticker, targetPrice)
        }
    }

    fun scheduleAlerts(context: Context) {
        val request = PeriodicWorkRequestBuilder<PriceAlertWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "price_alerts",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    suspend fun getChartData(ticker: String, period: String): List<Float> {
        return repo.getChartData(ticker, period)
    }
}