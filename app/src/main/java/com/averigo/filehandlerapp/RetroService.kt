package com.averigo.filehandlerapp

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface RetroService {

    @Multipart
    @POST("index.php/file-upload-mobile")
    fun uploadFile(
        @Header("Token") token: String,
        @Part file: MultipartBody.Part
    ): Call<FileUploadResponse>

    @FormUrlEncoded
    @POST("index.php/file-download-mobile")
    fun downloadFile(
        @Header("Token") token: String,
        @Field("filename") filename: String
    ): Call<ResponseBody>

}