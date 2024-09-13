package com.example.sheerhealthinterview.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

object SheerAPI {
    private const val BASE_URL = "https://android-project-465819884967.us-central1.run.app"

    private fun okhttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit = Retrofit.Builder()
        .client(okhttpClient())
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(BASE_URL)
        .build()

    val retrofitService: SheerApiService by lazy {
        retrofit.create(SheerApiService::class.java)
    }
}

private class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        requestBuilder.addHeader("Authorization", "1")

        return chain.proceed(requestBuilder.build())
    }
}

interface SheerApiService {
    @GET("case")
    suspend fun getCases(): retrofit2.Response<List<Case>>

    @GET("case/{caseId}")
    suspend fun getDetails(@Path("caseId") caseId: String): retrofit2.Response<CaseDetails>
}
