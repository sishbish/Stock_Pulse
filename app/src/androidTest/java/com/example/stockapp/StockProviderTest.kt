package com.example.stockapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StockProviderTest {

    private lateinit var context: Context
    private val contentUri: Uri = StockProvider.CONTENT_URI

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Clear all database entries via provider deletion before running each test case
        context.contentResolver.delete(contentUri, null, null)
    }

    @Test
    fun testInsertAndQueryStock() {
        // Explicitly scoping constants to StockProvider to fix compilation failure
        val values = ContentValues().apply {
            put(StockProvider.COL_TICKER, "AAPL")
            put(StockProvider.COL_COMPANY_NAME, "Apple Inc.")
            put(StockProvider.COL_LAST_PRICE, 175.0)
            put(StockProvider.COL_CHANGE_PERCENT, "+1.2%")
        }

        val insertedUri = context.contentResolver.insert(contentUri, values)
        assertNotNull("Inserted URI should not be null", insertedUri)

        val cursor: Cursor? = context.contentResolver.query(contentUri, null, null, null, null)
        assertNotNull("Cursor should not be null", cursor)

        cursor?.use {
            assertTrue("Cursor should have at least one row", it.moveToFirst())
            assertEquals("AAPL", it.getString(it.getColumnIndexOrThrow(StockProvider.COL_TICKER)))
            assertEquals("Apple Inc.", it.getString(it.getColumnIndexOrThrow(StockProvider.COL_COMPANY_NAME)))
            assertEquals(175.0, it.getDouble(it.getColumnIndexOrThrow(StockProvider.COL_LAST_PRICE)), 0.001)
            assertEquals("+1.2%", it.getString(it.getColumnIndexOrThrow(StockProvider.COL_CHANGE_PERCENT)))
        }
    }

    @Test
    fun testDeleteStock() {
        val values = ContentValues().apply {
            put(StockProvider.COL_TICKER, "TSLA")
            put(StockProvider.COL_COMPANY_NAME, "Tesla Inc.")
            put(StockProvider.COL_LAST_PRICE, 200.0)
            put(StockProvider.COL_CHANGE_PERCENT, "-0.5%")
        }

        context.contentResolver.insert(contentUri, values)

        // Delete a specific stock item by passing target identifier bounds
        val deletedRows = context.contentResolver.delete(contentUri, "ticker = ?", arrayOf("TSLA"))
        assertEquals(1, deletedRows)

        val cursor: Cursor? = context.contentResolver.query(contentUri, null, null, null, null)
        cursor?.use {
            var found = false
            while (it.moveToNext()) {
                if (it.getString(it.getColumnIndexOrThrow(StockProvider.COL_TICKER)) == "TSLA") {
                    found = true
                }
            }
            assertFalse("Deleted stock should not be present in cursor mapping", found)
        }
    }

    @Test
    fun testBulkDelete() {
        val stock1 = ContentValues().apply {
            put(StockProvider.COL_TICKER, "MSFT")
            put(StockProvider.COL_COMPANY_NAME, "Microsoft Corp.")
            put(StockProvider.COL_LAST_PRICE, 400.0)
            put(StockProvider.COL_CHANGE_PERCENT, "+0.8%")
        }
        context.contentResolver.insert(contentUri, stock1)

        // Null criteria flags a full collection sweep / truncation event
        val deletedRows = context.contentResolver.delete(contentUri, null, null)
        assertTrue("Should delete at least one stock row item", deletedRows >= 1)

        val cursor: Cursor? = context.contentResolver.query(contentUri, null, null, null, null)
        cursor?.use {
            assertEquals("Cursor should be completely empty after a clear action", 0, it.count)
        }
    }
}