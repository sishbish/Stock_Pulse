package com.example.stockapp

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

//Retrofit interface for using the OpenRouter AI API.
//NOTE: I did try to use Gemini initially but it was instantly using all my daily usage in one request so I chose to use this free LLM instead.
//NOTE: The free models used with this API seem to change quite often so there is a likely chance the model will not work when used after submission.
interface OpenRouterApi {

//    Posts to the endpoint with an authorisation header
//    Defines the request and response data classes
    @POST("api/v1/chat/completions")
    suspend fun analyze(
        @Header("Authorization") auth: String = "API_KEY",
        @Body request: OpenRouterRequest
    ): OpenRouterResponse
}


data class OpenRouterRequest(
//    model being used currently
    val model: String = "google/gemma-4-31b-it:free",
    val messages: List<OpenRouterMessage>
)

//API response comes in this format at OpenRouterMessage wrapped in OpenRouterResponse wrapped in OpenRouterMessage
//message data class
data class OpenRouterMessage(
    val role: String,
    val content: String
)

//response data class
data class OpenRouterResponse(
    val choices: List<OpenRouterChoice>
)

//choice data class
data class OpenRouterChoice(
    val message: OpenRouterMessage
)