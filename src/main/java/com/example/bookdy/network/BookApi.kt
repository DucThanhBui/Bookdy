package com.example.bookdy.network

import com.example.bookdy.data.modeljson.BookJson
import com.example.bookdy.data.modeljson.ResponseMessage
import com.example.bookdy.data.modeljson.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface BookApi {
    @FormUrlEncoded
    @POST("/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "password", // Default
        @Field("scope") scope: String = "", // Optional
        @Field("client_id") clientId: String? = null, // Optional
        @Field("client_secret") clientSecret: String? = null // Optional
    ): ResponseMessage

    @POST("/register")
    suspend fun register(
        @Body user: User
    ): ResponseMessage

    @POST("/change-password")
    @FormUrlEncoded
    suspend fun changePassword(
        @Field("current_password") currentPassword: String,
        @Field("new_password") newPassword: String,
        @Header("Authorization") token: String
    ): ResponseMessage

    @Multipart
    @POST("/upload")
    suspend fun uploadBook(
        @Part file: MultipartBody.Part
    ) : ResponseMessage

    @POST("/upload-info")
    suspend fun uploadBookInfo(
        @Header("Authorization") token: String,
        @Body bookInfo: BookJson
    ) : ResponseMessage

    @GET("/files")
    suspend fun getAllFiles(
        @Header("Authorization") token: String
    ): List<BookJson>

    @GET("/file")
    suspend fun getFile(
        @Header("Authorization") token: String,
        @Query("identifier") identifier: String
    ): ResponseMessage

    @PATCH("/update-file")
    suspend fun updateFileInfo(
        @Header("Authorization") token: String,
        @Body fileInfo: BookJson
    ): ResponseMessage

    @DELETE
    suspend fun deleteBook(
        @Header("Authorization") token: String,
        @Query("identifier") identifier: String
    )
}