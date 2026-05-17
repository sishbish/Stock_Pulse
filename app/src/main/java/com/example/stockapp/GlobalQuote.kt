package com.example.stockapp

import com.google.gson.annotations.SerializedName

data class GlobalQuote(
    @SerializedName("01. symbol") val symbol: String,
    @SerializedName("05. price") val price: String,
    @SerializedName("03. high") val high: String,
    @SerializedName("04. low") val low: String,
    @SerializedName("02. open") val open: String,
    @SerializedName("06. volume") val volume: String,
    @SerializedName("10. change percent") val changePercent: String
)