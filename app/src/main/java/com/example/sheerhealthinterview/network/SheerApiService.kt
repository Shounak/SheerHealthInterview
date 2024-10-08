package com.example.sheerhealthinterview.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
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
        .addConverterFactory(generateConverterFactory())
        .baseUrl(BASE_URL)
        .build()

    private fun generateConverterFactory(): Converter.Factory {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        val contentType = "application/json".toMediaType()
        return json.asConverterFactory(contentType)
    }

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
    suspend fun getCases(): retrofit2.Response<MutableList<Case>>

    @POST("case")
    suspend fun createCase(@Body newCase: NewCase): retrofit2.Response<Case>

    @DELETE("case/{caseId}")
    suspend fun deleteCase(@Path("caseId") caseId: String): retrofit2.Response<String>

    @GET("case/{caseId}")
    suspend fun getDetails(@Path("caseId") caseId: String): retrofit2.Response<CaseDetails>

    @DELETE("case/{caseId}/detail/{detailId}")
    suspend fun deleteDetail(@Path("caseId") caseId: String, @Path("detailId") detailId: String): retrofit2.Response<String>

    @POST("case/{caseId}/detail")
    suspend fun createDetail(@Path("caseId") caseId: String, @Body newDetail: NewDetail): retrofit2.Response<Detail>
}
