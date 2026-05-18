package com.example.stockapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    stock: StockEntity,
    onBackClick: () -> Unit,
    onExternalViewClick: () -> Unit,
    onAiAnalysisClick: () -> Unit,
    onShareClick: () -> Unit,
    onConfirmPriceAlert: (Double) -> Unit // UPDATED: Changed from click listener to data emitter callback
) {
    var selectedTimeframe by remember { mutableStateOf("1D") }
    val isNegative = stock.changePercent.contains("-")

    // DIALOG STATE TRACKERS: Handles visibility and text processing safely
    var showAlertDialog by remember { mutableStateOf(false) }
    var alertPriceInput by remember { mutableStateOf("") }

    val openPrice = stock.open.toFloat()
    val highPrice = stock.high.toFloat()
    val lowPrice = stock.low.toFloat()
    val lastPrice = stock.lastPrice.toFloat()

    val isChartDataValid = highPrice > 0f && lowPrice > 0f && highPrice != lowPrice

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(text = "Back", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    // Clicking the toolbar icon toggles the popup visible
                    IconButton(onClick = { showAlertDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Set Price Alert",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Brand Header Section
            Text(
                text = stock.companyName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = stock.ticker.uppercase(),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Stock",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Core Price Displays
            Text(
                text = "$${String.format("%.2f", stock.lastPrice)}",
                fontSize = 54.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = if (isNegative) "▼ " else "▲ ",
                    fontSize = 12.sp,
                    color = if (isNegative) MaterialTheme.colorScheme.error else CustomGreen
                )
                Text(
                    text = "${stock.changePercent} today",
                    fontSize = 13.sp,
                    color = if (isNegative) MaterialTheme.colorScheme.error else CustomGreen,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Multi-Line Trend Chart Context
            if (isChartDataValid) {
                DetailedMultiLineChart(
                    open = openPrice,
                    high = highPrice,
                    low = lowPrice,
                    close = lastPrice,
                    timeframe = selectedTimeframe
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Historical chart data temporarily unavailable.\nCheck API rate limits or connection.",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Timeframe Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("1D", "1M", "1Y").forEach { timeframe ->
                    val isSelected = selectedTimeframe == timeframe
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .background(
                                color = if (isSelected) Color(0x26FFFFFF) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedTimeframe = timeframe }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = timeframe,
                            color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Statistics Grid Layout
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    GridStatRow(label = "Open:", value = "$${String.format("%.2f", stock.open)}")
                    GridStatRow(label = "High:", value = "$${String.format("%.2f", stock.high)}")
                    GridStatRow(label = "Low:", value = "$${String.format("%.2f", stock.low)}")
                }
                Spacer(modifier = Modifier.width(24.dp))
                Column(modifier = Modifier.weight(1f)) {
                    GridStatRow(label = "Volume:", value = stock.volume)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Price Range Slider Section
            Text(
                text = "Today's price range",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "$${stock.low}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)

                val currentRangeSpan = highPrice - lowPrice
                val thumbPosition = if (currentRangeSpan > 0f) (lastPrice - lowPrice) / currentRangeSpan else 0.5f

                Slider(
                    value = thumbPosition.coerceIn(0f, 1f),
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        disabledThumbColor = Color.LightGray,
                        disabledActiveTrackColor = MaterialTheme.colorScheme.onBackground,
                        disabledInactiveTrackColor = Color.DarkGray
                    )
                )

                Text(text = "$${stock.high}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Footer Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onExternalViewClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Read Full News", color = Color.Black, fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = onAiAnalysisClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .padding(start = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("AI Analysis", color = Color.Black, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    // POPUP WINDOW CONTENT OVERLAY: Triggers when showAlertDialog is true
    if (showAlertDialog) {
        AlertDialog(
            onDismissRequest = { showAlertDialog = false },
            title = {
                Text(
                    text = "Set Target Price Alert",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter the price barrier tracking goal threshold for ${stock.ticker.uppercase()}:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = alertPriceInput,
                        onValueChange = { alertPriceInput = it },
                        label = { Text("Target Price ($)") },
                        singleLine = true,
                        // Configures the keyboard to show numbers and decimals only
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedPrice = alertPriceInput.toDoubleOrNull()
                        if (parsedPrice != null && parsedPrice > 0.0) {
                            onConfirmPriceAlert(parsedPrice)
                            showAlertDialog = false // Dismiss popup window
                            alertPriceInput = ""    // Clear buffer memory state
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Confirm Alert", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAlertDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface // Inherits the dark charcoal background
        )
    }
}

@Composable
fun GridStatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(text = value, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DetailedMultiLineChart(open: Float, high: Float, low: Float, close: Float, timeframe: String) {
    val multiplier = when (timeframe) {
        "1M" -> 1.15f
        "1Y" -> 1.45f
        else -> 1.00f
    }

    val path1Data = listOf(open, (open + high) / 2f * multiplier, high * multiplier, (high + close) / 2f, close)
    val path2Data = listOf(open, low / multiplier, (low + close) / 2f, high, close * multiplier)
    val path3Data = listOf(open, (open + low) / 2f * multiplier, low, (low + high) / 2f * multiplier, close)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(vertical = 8.dp)
    ) {
        val absoluteMax = maxOf(open, high * multiplier, low, close * multiplier)
        val absoluteMin = minOf(open, high, low / multiplier, close)

        drawLivePath(path1Data, Color(0xFFFFAB91), absoluteMax, absoluteMin)
        drawLivePath(path2Data, Color(0xFF80DEEA), absoluteMax, absoluteMin)
        drawLivePath(path3Data, Color(0xFFC5CAE9), absoluteMax, absoluteMin)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLivePath(
    points: List<Float>,
    color: Color,
    maxVal: Float,
    minVal: Float
) {
    val deltaY = maxVal - minVal
    val distanceX = size.width / (points.size - 1)

    val path = Path()
    val nodeRadius = 4.dp.toPx()

    points.forEachIndexed { index, value ->
        val currentX = index * distanceX
        val normalizedY = if (deltaY > 0f) (value - minVal) / deltaY else 0.5f
        val currentY = size.height - (normalizedY * size.height)

        if (index == 0) {
            path.moveTo(currentX, currentY)
        } else {
            path.lineTo(currentX, currentY)
        }

        drawCircle(
            color = Color.White,
            radius = nodeRadius,
            center = Offset(currentX, currentY)
        )
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 2.dp.toPx())
    )
}

private fun Int.bindPx(): Dp = this.dp