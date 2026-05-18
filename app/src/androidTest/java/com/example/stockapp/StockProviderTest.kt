package com.example.stockapp

import android.content.ContentValues
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

//instrumented tests that tests the content provider
@RunWith(AndroidJUnit4::class)
class StockProviderTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val contentResolver = context.contentResolver

    @Before
    fun setup() {
        // clear database before each test to ensure clean state
        contentResolver.delete(StockProvider.CONTENT_URI, null, null)
    }

//    inserts a row and checks the returned URI
    @Test
    fun testInsert() {
        val values = ContentValues().apply {
            put(StockProvider.COL_TICKER, "TSLA")
            put(StockProvider.COL_COMPANY_NAME, "Tesla Inc")
            put(StockProvider.COL_LAST_PRICE, 250.0)
            put(StockProvider.COL_CHANGE_PERCENT, "+1.50%")
        }
        val uri = contentResolver.insert(StockProvider.CONTENT_URI, values)
        assertNotNull("Insert should return a URI", uri)
    }

//    checks the inserted row appears in the cursor
    @Test
    fun testQuery() {
        val values = ContentValues().apply {
            put(StockProvider.COL_TICKER, "AAPL")
            put(StockProvider.COL_COMPANY_NAME, "Apple Inc")
            put(StockProvider.COL_LAST_PRICE, 175.0)
            put(StockProvider.COL_CHANGE_PERCENT, "+0.50%")
        }
        contentResolver.insert(StockProvider.CONTENT_URI, values)

        val cursor = contentResolver.query(
            StockProvider.CONTENT_URI, null, null, null, null
        )
        assertNotNull("Cursor should not be null", cursor)
        assertTrue("Cursor should have at least one row", cursor!!.count > 0)

        // find AAPL in the cursor rather than assuming first row
        var found = false
        while (cursor.moveToNext()) {
            val tickerIndex = cursor.getColumnIndex(StockProvider.COL_TICKER)
            if (cursor.getString(tickerIndex) == "AAPL") {
                found = true
                break
            }
        }
        cursor.close()
        assertTrue("AAPL should be in the cursor", found)
    }

//    checks a row can be removed by ticker
    @Test
    fun testDelete() {
        // insert a stock first
        val values = ContentValues().apply {
            put(StockProvider.COL_TICKER, "MSFT")
            put(StockProvider.COL_COMPANY_NAME, "Microsoft Corp")
            put(StockProvider.COL_LAST_PRICE, 420.0)
            put(StockProvider.COL_CHANGE_PERCENT, "+1.25%")
        }
        contentResolver.insert(StockProvider.CONTENT_URI, values)

        // delete the stock
        val rowsDeleted = contentResolver.delete(
            StockProvider.CONTENT_URI, null, arrayOf("MSFT")
        )
        assertEquals("One row should be deleted", 1, rowsDeleted)

        // verify it's gone
        val cursor = contentResolver.query(
            StockProvider.CONTENT_URI, null, null, null, null
        )
        cursor?.moveToFirst()
        var found = false
        while (cursor != null && !cursor.isAfterLast) {
            val tickerIndex = cursor.getColumnIndex(StockProvider.COL_TICKER)
            if (cursor.getString(tickerIndex) == "MSFT") {
                found = true
                break
            }
            cursor.moveToNext()
        }
        cursor?.close()
        assertFalse("MSFT should not be in the database", found)
    }
}