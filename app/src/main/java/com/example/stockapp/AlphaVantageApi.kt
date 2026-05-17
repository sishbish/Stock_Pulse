package com.example.stockapp
import retrofit2.http.GET
import retrofit2.http.Query

interface AlphaVantageApi {
    @GET("query")
    suspend fun getQuote(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String = "API_KEY"
    ): GlobalQuoteResponse

    @GET("query")
    suspend fun getIntraday(
        @Query("function") function: String = "TIME_SERIES_INTRADAY",
        @Query("symbol") symbol: String,
        @Query("interval") interval: String = "5min",
        @Query("apikey") apiKey: String = "API_KEY"
    ): IntradayResponse

    @GET("query")
    suspend fun getDaily(
        @Query("function") function: String = "TIME_SERIES_DAILY",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String = "API_KEY"
    ): DailyResponse
}