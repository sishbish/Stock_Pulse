package com.example.stockapp

import com.google.gson.annotations.SerializedName

data class GlobalQuoteResponse(
    @SerializedName("Global Quote") val globalQuote: GlobalQuote
)