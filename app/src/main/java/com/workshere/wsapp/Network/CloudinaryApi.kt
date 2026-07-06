package com.workshere.wsapp.Network

import com.workshere.wsapp.Model.CloudinaryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CloudinaryApi {
    @Multipart
    @POST("v1_1/dsyxjpsbl/image/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("ws_image") uploadPreset: RequestBody
    ): Response<CloudinaryResponse>
}