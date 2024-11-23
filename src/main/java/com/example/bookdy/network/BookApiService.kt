package com.example.bookdy.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BookApiService {
    private const val BASE_URL = "http://192.168.0.11:8000/"

    private val gsonConverterFactory = GsonConverterFactory.create()


    private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(gsonConverterFactory)
            .build()


    val retrofitService: BookApi by lazy {
        retrofit.create(BookApi::class.java)
    }
}