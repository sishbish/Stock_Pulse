package com.example.stockapp

// 3RD-PARTY LIBRARIES USED:
// 1. Jetpack Compose UI & Foundation - Handles layouts, padding modifiers, and click events.
// 2. Jetpack Compose Material 3 - Provides pre-styled themes, typography styles, and Card components.
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StockItemRow(
    stock: StockEntity,
    onItemClick: (StockEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onItemClick(stock) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
//             left side column: Ticker and Company Name
            Column {
                Text(
                    text = stock.ticker,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stock.companyName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

//             right side column: Price and Percentage Change
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.2f", stock.lastPrice)}",
                    style = MaterialTheme.typography.titleMedium
                )

//                 red for negative change, green for positive
                val isNegative = stock.changePercent.contains("-")
                Text(
                    text = stock.changePercent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isNegative) Color.Red else Color(0xFF388E3C)
                )
            }
        }
    }
}