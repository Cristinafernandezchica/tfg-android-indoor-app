package com.cristina.tfg_android_indoor_app.data.remote

import RoomApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(USERS_API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)

    val trainingApi: RoomApi = Retrofit.Builder()
        .baseUrl(ROOMS_API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RoomApi::class.java)


    val mlApi: MLApi = Retrofit.Builder()
        .baseUrl(ROOMS_API_BASE_URL)   // misma API que rooms/sensors
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MLApi::class.java)



}

