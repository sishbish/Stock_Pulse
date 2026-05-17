package com.example.stockapp
import retrofit2.http.GET
import retrofit2.http.Query


//Uses retrofit to call the Alpha Vantage API
interface AlphaVantageApi {

//    uses the GLOBAL_QUOTE function to get the current price data
    @GET("query")
    suspend fun getQuote(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String = "API_KEY"
    ): GlobalQuoteResponse

//    uses the TIME_SERIES_INTRADAY function with a 5 min interval between data points
    @GET("query")
    suspend fun getIntraday(
        @Query("function") function: String = "TIME_SERIES_INTRADAY",
        @Query("symbol") symbol: String,
        @Query("interval") interval: String = "5min",
        @Query("apikey") apiKey: String = "API_KEY"
    ): IntradayResponse

//    uses the TIME_SERIES_DAILY function
    @GET("query")
    suspend fun getDaily(
        @Query("function") function: String = "TIME_SERIES_DAILY",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String = "API_KEY"
    ): DailyResponse
}