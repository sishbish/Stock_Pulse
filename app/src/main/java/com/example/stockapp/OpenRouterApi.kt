package com.example.stockapp

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApi {
    @POST("api/v1/chat/completions")
    suspend fun analyze(
        @Header("Authorization") auth: String = "API_KEY",
        @Body request: OpenRouterRequest
    ): OpenRouterResponse
}

data class OpenRouterRequest(
    val model: String = "google/gemma-4-31b-it:free",
    val messages: List<OpenRouterMessage>
)

data class OpenRouterMessage(
    val role: String,
    val content: String
)

data class OpenRouterResponse(
    val choices: List<OpenRouterChoice>
)

data class OpenRouterChoice(
    val message: OpenRouterMessage
)