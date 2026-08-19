package com.example.kpop.network

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Android Studio Emulator -> PC Spring Boot
    const val BASE_URL = "http://10.1.100.21:8080/"

    @Volatile
    private var retrofit: Retrofit? = null

    private fun getRetrofit(context: Context): Retrofit {

        return retrofit ?: synchronized(this) {

            retrofit ?: buildRetrofit(
                context.applicationContext
            ).also {
                retrofit = it
            }
        }
    }

    private fun buildRetrofit(context: Context): Retrofit {

        val sessionManager = SessionManager(context)

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->

                val originalRequest = chain.request()

                // Correct syntax for your OkHttp version
                val path = originalRequest.url().encodedPath()
                val method = originalRequest.method()

                val publicRequest = isPublicRequest(
                    method,
                    path
                )

                val requestBuilder =
                    originalRequest.newBuilder()

                var tokenWasSent = false

                // Only send JWT to protected APIs
                if (!publicRequest) {

                    val token = sessionManager.getToken()

                    if (!token.isNullOrBlank()) {

                        requestBuilder.header(
                            "Authorization",
                            "Bearer $token"
                        )

                        tokenWasSent = true
                    }
                }

                val response =
                    chain.proceed(requestBuilder.build())

                // Token is expired / invalid
                if (
                    tokenWasSent &&
                    response.code() == 401
                ) {
                    sessionManager.logout()
                }

                response
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .client(client)
            .build()
    }

    private fun isPublicRequest(
        method: String,
        path: String
    ): Boolean {

        // Login does not need JWT
        if (path == "/users/login") {
            return true
        }

        // Register does not need JWT
        if (path == "/users/register") {
            return true
        }

        // These public endpoints are GET only
        if (method != "GET") {
            return false
        }

        return isPath(path, "/products") ||
                isPath(path, "/groups") ||
                isPath(path, "/categories") ||
                isPath(path, "/variants") ||
                isPath(path, "/product-images") ||
                isPath(path, "/reviews/product") ||
                isPath(path, "/uploads")
    }

    private fun isPath(
        path: String,
        basePath: String
    ): Boolean {

        return path == basePath ||
                path.startsWith("$basePath/")
    }

    fun <T> create(
        context: Context,
        service: Class<T>
    ): T {

        return getRetrofit(
            context.applicationContext
        ).create(service)
    }

    fun imageUrl(url: String?): String? {

        if (url.isNullOrBlank()) {
            return null
        }

        if (
            url.startsWith("http://") ||
            url.startsWith("https://")
        ) {
            return url
        }

        return BASE_URL.removeSuffix("/") +
                "/" +
                url.removePrefix("/")
    }
}