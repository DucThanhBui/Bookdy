package com.example.bookdy.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object BookApiService {
    private const val BASE_URL = "http://127.0.0.1/"

     private val okHttpClient = OkHttpClient().newBuilder()
            .readTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .build()


    private val gsonConverterFactory = GsonConverterFactory.create()


    private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()


    val retrofitService: BookApi by lazy {
        retrofit.create(BookApi::class.java)
    }
}