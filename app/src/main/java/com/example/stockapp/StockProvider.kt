package com.example.stockapp

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri

class StockProvider : ContentProvider() {

    private lateinit var database: AppDatabase
    private lateinit var dao: StockDao

    companion object {
        private const val AUTHORITY = "com.example.stockapp.provider"
        private const val STOCKS_MATCH_CODE = 1

        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/stocks")

        // CRITICAL FIX: Explicitly expose column constant strings for StockProviderTest.kt
        const val COL_TICKER = "ticker"
        const val COL_COMPANY_NAME = "companyName"
        const val COL_LAST_PRICE = "lastPrice"
        const val COL_CHANGE_PERCENT = "changePercent"

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "stocks", STOCKS_MATCH_CODE)
        }
    }

    override fun onCreate(): Boolean {
        val ctx = context ?: return false
        database = AppDatabase.getDatabase(ctx)
        dao = database.stockDao()
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            STOCKS_MATCH_CODE -> {
                dao.getAllStocksCursor()
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (uriMatcher.match(uri) != STOCKS_MATCH_CODE || values == null) {
            throw IllegalArgumentException("Invalid insertion URI or missing values")
        }

        val ticker = values.getAsString(COL_TICKER) ?: return null
        val entity = StockEntity(
            ticker = ticker,
            companyName = values.getAsString(COL_COMPANY_NAME) ?: "",
            lastPrice = values.getAsDouble(COL_LAST_PRICE) ?: 0.0,
            changePercent = values.getAsString(COL_CHANGE_PERCENT) ?: "0.00%",
            open = values.getAsDouble("open") ?: 0.0,
            high = values.getAsDouble("high") ?: 0.0,
            low = values.getAsDouble("low") ?: 0.0,
            volume = values.getAsString("volume") ?: "0"
        )

        // CRITICAL FIX: Safely insert into the Room database so testQuery can retrieve it
        dao.insertStockSync(entity)

        return Uri.withAppendedPath(CONTENT_URI, ticker)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        if (uriMatcher.match(uri) != STOCKS_MATCH_CODE) {
            throw IllegalArgumentException("Unknown URI: $uri")
        }

        // CRITICAL FIX: Add simple matching logic using target selection variables requested by tests
        return if (selectionArgs != null && selectionArgs.isNotEmpty()) {
            val tickerToDelete = selectionArgs[0]
            dao.deleteByTickerSync(tickerToDelete)
        } else {
            // Bulk delete fallback when selection constraints are null
            dao.clearAllStocksSync()
        }
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun getType(uri: Uri): String? = "vnd.android.cursor.dir/vnd.$AUTHORITY.stocks"
}