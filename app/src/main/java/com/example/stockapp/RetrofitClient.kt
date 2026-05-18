package com.example.stockapp

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
// 3RD-PARTY LIBRARIES USED:
// 1. Square OkHttp3 (okhttp3.OkHttpClient, okhttp3.logging.HttpLoggingInterceptor) - Used for handling HTTP requests and network logging.
// 2. Square Retrofit2 (retrofit2.Retrofit) - Used to build the REST client API.
// 3. Square Retrofit2 Gson Converter (retrofit2.converter.gson.GsonConverterFactory) - Used for JSON serialisation/deserialisation.

//object that constructs the Retrofit instance for Alpha Vantage
//HttpLoggingInterceptor used for debugging
object RetrofitClient {


    const val API_KEY = "NXM4HQDA9KN712QX"

    val api: AlphaVantageApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://www.alphavantage.co/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AlphaVantageApi::class.java)
    }
}