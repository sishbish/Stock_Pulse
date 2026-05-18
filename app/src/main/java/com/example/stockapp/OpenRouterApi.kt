package com.example.stockapp

// 3RD-PARTY LIBRARIES USED:
// 1. Retrofit (retrofit2.http.*) - Handles declaration of asynchronous network endpoints.
// 2. OkHttp3 RequestBody - Accepts raw structured text streams over server channels.
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApi {
    @POST("api/v1/chat/completions")
    suspend fun getCompletion(
        @Header("Authorization") authHeader: String,
        @Body body: RequestBody
    ): OpenRouterResponse
}

// --- Data Models matching the expected JSON response format ---

data class OpenRouterResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: MessageContent
)

data class MessageContent(
    val role: String,
    val content: String
)