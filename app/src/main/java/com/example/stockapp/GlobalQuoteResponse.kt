package com.example.stockapp

import com.google.gson.annotations.SerializedName

//Contains the GlobalQuote object
data class GlobalQuoteResponse(
    @SerializedName("Global Quote") val globalQuote: GlobalQuote
)