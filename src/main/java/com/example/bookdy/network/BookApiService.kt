package com.example.bookdy.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BookApiService {
    private const val BASE_URL = "http://192.168.0.11:8000/"

    private val gsonConverterFactory = GsonConverterFactory.create()

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()


    val retrofitService: BookApi by lazy {
        retrofit.create(BookApi::class.java)
    }
}