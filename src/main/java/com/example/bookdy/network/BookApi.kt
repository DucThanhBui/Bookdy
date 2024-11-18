package com.example.bookdy.network

import com.example.bookdy.data.modeljson.BookJson
import com.example.bookdy.data.modeljson.ResponseMessage
import com.example.bookdy.data.modeljson.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap

interface BookApi {
    @POST
    fun login(@Body user: User): ResponseMessage


    @GET("/recipes/complexSearch")
    suspend fun getAllBooks(
        @Body user: User
    ) : List<BookJson>
}