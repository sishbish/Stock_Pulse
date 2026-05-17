package com.example.stockapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//Retrofit client for OpenRouter
object OpenRouterClient {
    val api: OpenRouterApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://openrouter.ai/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenRouterApi::class.java)
    }
}