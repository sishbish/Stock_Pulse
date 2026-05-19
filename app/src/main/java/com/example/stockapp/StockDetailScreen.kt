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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    stock: StockEntity,
    onFetchChartData: suspend (String) -> List<Pair<String, Float>>,
    onBackClick: () -> Unit,
    onExternalViewClick: () -> Unit,
    onAiAnalysisClick: () -> Unit,
    onShareClick: () -> Unit,
    onConfirmPriceAlert: (Double) -> Unit
) {
//    tracks whether the user selected 1D, 1M, 1Y
    var selectedTimeframe by remember { mutableStateOf("1D") }
//    Checks if stock had a negative return so that the colour changes accordingly
    val isNegative = stock.changePercent.contains("-")

//    tracks if the popup box should be visible
    var showAlertDialog by remember { mutableStateOf(false) }
//    price alert value
    var alertPriceInput by remember { mutableStateOf("") }

    // holds the real list of prices fetched from the web API.
    var chartPoints by remember { mutableStateOf<List<Pair<String, Float>>>(emptyList()) }
    var isLoadingChart by remember { mutableStateOf(true) }

//     automatically runs in the background whenever the user
//     opens this page or clicks a different timeframe button 1D, 1M, 1Y
    LaunchedEffect(selectedTimeframe) {
        isLoadingChart = true
        // calls Alpha Vantage getChartData repository function asynchronously
        val fetchedPoints = onFetchChartData(selectedTimeframe)
        chartPoints = fetchedPoints
        isLoadingChart = false
    }

//    converts stock prices into numbers
    val openPrice = stock.open.toFloat()
    val highPrice = stock.high.toFloat()
    val lowPrice = stock.low.toFloat()
    val lastPrice = stock.lastPrice.toFloat()

//    checks if the values are valid
    val isChartDataValid = highPrice > 0f && lowPrice > 0f && highPrice != lowPrice


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
//        for toolbar header
        topBar = {
            TopAppBar(
                title = { Text(text = "Back", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp) },
                navigationIcon = {
//                    backwards navigation
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
//                     Clicking the toolbar icon toggles the popup visible
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
//        column layout structure
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

//             brand Header Section
            Text(
                text = stock.companyName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

//            places the stock ticker and share button
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

//                share icon button
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

//             price Displays
            Text(
                text = "$${String.format("%.2f", stock.lastPrice)}",
                fontSize = 54.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground
            )

//            green up arrow or red down arrow depending on price change
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = if (isNegative) "▼ " else "▲ ",
                    fontSize = 12.sp,
                    color = if (isNegative) MaterialTheme.colorScheme.error else Color(0xFF388E3C)
                )
                Text(
                    text = "${stock.changePercent} today",
                    fontSize = 13.sp,
                    color = if (isNegative) MaterialTheme.colorScheme.error else Color(0xFF388E3C),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

//            If there is valid API info then the chart is displayed
//            If there isnt then show error message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoadingChart) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else if (chartPoints.isNotEmpty()) {
                    SingleLineStockChart(
                        points = chartPoints.map { it.second },
                        dates = chartPoints.map { it.first },
                        isNegative = isNegative
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Check API rate limits or connection.",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

//             Interactive Timeframe Selector, 1D, 1M, 1Y
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
//                            changes timeframe when clicked
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

//             Statistics Grid Layout
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

//             Price Range Slider Section
            Text(
                text = "Today's price range",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

//            range slider bar. Shows the daily high/low values and positions the slider to show
//            where the current price sits between the two values
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "$${stock.low}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)

//                calculating current percentage position on the bar
                val currentRangeSpan = highPrice - lowPrice
                val thumbPosition = if (currentRangeSpan > 0f) (lastPrice - lowPrice) / currentRangeSpan else 0.5f

//                slider layout but turned off so user cant slide the bar
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

//             Footer Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
//                read full news button
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

//                ai analysis button
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

//    popup alert window
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
//                    text input box for price alert value
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
//                confirm action button
                Button(
                    onClick = {
                        val parsedPrice = alertPriceInput.toDoubleOrNull()
//                        validating the input value
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
//                cancel button to back out of alert popup
                TextButton(onClick = { showAlertDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface // Inherits the dark charcoal background
        )
    }
}

//custom design block for the stats grid. Makes sure the stat labels and values are always inline
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

//creates line graph paths using arrays directly retrieved from the api
@Composable
fun SingleLineStockChart(
    points: List<Float>,
    dates: List<String>,
    isNegative: Boolean,
    modifier: Modifier = Modifier
) {
    val lineBrushColor = if (isNegative) MaterialTheme.colorScheme.error else Color(0xFF388E3C)
    val axisColor = Color(0xFF888888)
    val labelColor = Color(0xFFAAAAAA)

//     format a "YYYY-MM-DD" date string down to "MMM DD"
    fun formatDate(raw: String): String {
        return try {
            val parts = raw.split("-")
            val months = listOf("Jan","Feb","Mar","Apr","May","Jun",
                "Jul","Aug","Sep","Oct","Nov","Dec")
            val month = months.getOrElse(parts[1].toInt() - 1) { "" }
            "$month ${parts[2]}"
        } catch (e: Exception) { raw }
    }

//     build the three X-axis label strings: first, middle, last date
    val xLabels: List<String> = if (dates.size >= 2) {
        val first = formatDate(dates.first())
        val mid   = formatDate(dates[dates.size / 2])
        val last  = formatDate(dates.last())
        listOf(first, mid, last)
    } else emptyList()

    val yAxisWidthDp = 52.dp
    val xAxisHeightDp = 20.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
//         Y-axis
        val maxVal = points.maxOrNull() ?: 1f
        val minVal = points.minOrNull() ?: 0f
        val midVal = (maxVal + minVal) / 2f

        Column(
            modifier = Modifier
                .width(yAxisWidthDp)
                .fillMaxHeight()
                .padding(bottom = xAxisHeightDp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(maxVal, midVal, minVal).forEach { value ->
                Text(
                    text = "$${String.format("%.2f", value)}",
                    color = labelColor,
                    fontSize = 9.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

//         X-axis
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp)
            ) {
                val deltaY = maxVal - minVal
                val distanceX = size.width / (if (points.size > 1) (points.size - 1) else 1).toFloat()
                val path = Path()
                val nodeRadius = 2.5.dp.toPx()

//                 horizontal gridlines at top, middle, bottom
                listOf(0f, 0.5f, 1f).forEach { fraction ->
                    val y = size.height * (1f - fraction)
                    drawLine(
                        color = axisColor.copy(alpha = 0.25f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                points.forEachIndexed { index, value ->
                    val currentX = index * distanceX
                    val normalizedY = if (deltaY > 0f) (value - minVal) / deltaY else 0.5f
                    val currentY = size.height - (normalizedY * size.height)

                    if (index == 0) path.moveTo(currentX, currentY)
                    else path.lineTo(currentX, currentY)

//                     Only draw dots at first and last point to keep it clean
                    if (index == 0 || index == points.lastIndex) {
                        drawCircle(
                            color = Color.White,
                            radius = nodeRadius,
                            center = Offset(currentX, currentY)
                        )
                    }
                }

                drawPath(path = path, color = lineBrushColor, style = Stroke(width = 2.5.dp.toPx()))

//                 Bottom axis line
                drawLine(
                    color = axisColor.copy(alpha = 0.4f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }

//             X-axis date labels
            if (xLabels.size == 3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(xAxisHeightDp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    xLabels.forEach { label ->
                        Text(
                            text = label,
                            color = labelColor,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}