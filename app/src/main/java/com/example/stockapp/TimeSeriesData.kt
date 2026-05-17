package com.example.stockapp

import com.google.gson.annotations.SerializedName

//Data classes for the time series responses
data class IntradayResponse(
    @SerializedName("Time Series (5min)") val timeSeries: Map<String, OHLCData>?
)

data class DailyResponse(
    @SerializedName("Time Series (Daily)") val timeSeries: Map<String, OHLCData>?
)

data class OHLCData(
    @SerializedName("1. open") val open: String,
    @SerializedName("2. high") val high: String,
    @SerializedName("3. low") val low: String,
    @SerializedName("4. close") val close: String,
    @SerializedName("5. volume") val volume: String
)