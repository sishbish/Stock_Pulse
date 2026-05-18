package com.example.stockapp

import android.content.*
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import kotlinx.coroutines.runBlocking

//content provider that exposes the watchlist to other apps
class StockProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.stockapp.provider"
        const val PATH_STOCKS = "stocks"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$PATH_STOCKS")

        const val COL_TICKER = "ticker"
        const val COL_COMPANY_NAME = "companyName"
        const val COL_LAST_PRICE = "lastPrice"
        const val COL_CHANGE_PERCENT = "changePercent"
    }

    private lateinit var db: AppDatabase

    override fun onCreate(): Boolean {
        db = AppDatabase.getDatabase(context!!)
        return true
    }

//    Uses query using runBlocking to call the Dao
    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val stocks = runBlocking { db.stockDao().getAllStocksSync() }
        val cursor = MatrixCursor(arrayOf(COL_TICKER, COL_COMPANY_NAME, COL_LAST_PRICE, COL_CHANGE_PERCENT))
        stocks.forEach { stock ->
            cursor.addRow(arrayOf(stock.ticker, stock.companyName, stock.lastPrice, stock.changePercent))
        }
        return cursor
    }

//    builds a stockEntity object from ContentValues
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        values ?: return null
        val stock = StockEntity(
            ticker = values.getAsString(COL_TICKER) ?: return null,
            companyName = values.getAsString(COL_COMPANY_NAME) ?: "",
            lastPrice = values.getAsDouble(COL_LAST_PRICE) ?: 0.0,
            changePercent = values.getAsString(COL_CHANGE_PERCENT) ?: "0.00%"
        )
        runBlocking { db.stockDao().insertStock(stock) }
        context?.contentResolver?.notifyChange(uri, null)
        return ContentUris.withAppendedId(CONTENT_URI, stock.ticker.hashCode().toLong())
    }

//    returns 0
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

//    either wipes all stocks or deletes a specific ticker
    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return if (selectionArgs == null) {
            // delete all stocks
            runBlocking {
                val stocks = db.stockDao().getAllStocksSync()
                stocks.forEach { db.stockDao().deleteStock(it) }
                stocks.size
            }
        } else {
            val ticker = selectionArgs.firstOrNull() ?: return 0
            val stock = runBlocking {
                db.stockDao().getAllStocksSync().find { it.ticker == ticker }
            } ?: return 0
            runBlocking { db.stockDao().deleteStock(stock) }
            context?.contentResolver?.notifyChange(uri, null)
            1
        }
    }

    override fun getType(uri: Uri): String = "vnd.android.cursor.dir/vnd.$AUTHORITY.$PATH_STOCKS"
}