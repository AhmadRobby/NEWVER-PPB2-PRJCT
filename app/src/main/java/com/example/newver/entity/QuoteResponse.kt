package com.example.newver.entity

data class QuoteResponse(
    val id: Int,
    val quote: String,  // Ini isi kata mutiaranya
    val author: String  // Ini nama tokohnya
)