package com.example.newver.api

import com.example.newver.entity.QuoteResponse
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    // Kita ambil quote secara acak
    @GET("quotes/random")
    suspend fun getRandomQuote(): Response<QuoteResponse>
}