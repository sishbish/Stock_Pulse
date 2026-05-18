package com.example.stockapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

//Fetches all stocks from room and checks to see if alert threshold has been met
class PriceAlertWorker(
    private val context: Context,
    params: WorkerParameters
//    coroutineWorker so that is can use suspend functions
) : CoroutineWorker(context, params) {


    override suspend fun doWork(): Result {
//        fetch all stocks on watchlist
        android.util.Log.d("PriceAlertWorker", "Worker started")
        val db = AppDatabase.getDatabase(context)
        val stocks = db.stockDao().getAllStocksSync()
        android.util.Log.d("PriceAlertWorker", "Found ${stocks.size} stocks")

        stocks.forEach { stock ->

            android.util.Log.d("PriceAlertWorker", "Checking ${stock.ticker}, target: ${stock.targetPrice}")
            val targetPrice = stock.targetPrice ?: return@forEach
            try {
                val response = RetrofitClient.api.getQuote(symbol = stock.ticker)
                val currentPrice = response.globalQuote.price.toDoubleOrNull() ?: return@forEach
                android.util.Log.d("PriceAlertWorker", "Current price: $currentPrice, target: $targetPrice")

//              sends notification if the stock price exceeds the target price
                if (currentPrice >= targetPrice) {
                    android.util.Log.d("PriceAlertWorker", "Sending notification for ${stock.ticker}")
                    sendNotification(stock.ticker, currentPrice, targetPrice)
                }

            } catch (e: Exception) {
                android.util.Log.e("PriceAlertWorker", "Error: ${e.message}")
            }
        }
        return Result.success()
    }

    private fun sendNotification(ticker: String, currentPrice: Double, targetPrice: Double) {
        val channelId = "price_alerts"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Price Alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Price Alert: $ticker")
            .setContentText("$ticker has reached $${"%.2f".format(currentPrice)} (target: $${"%.2f".format(targetPrice)})")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(ticker.hashCode(), notification)
    }
}