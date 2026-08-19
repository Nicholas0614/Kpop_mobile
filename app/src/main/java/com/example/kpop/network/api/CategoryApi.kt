package com.example.kpop.network.api

import com.example.kpop.network.model.ApiCategory
import com.example.kpop.network.model.CategoryRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CategoryApi {

    @GET("categories")
    fun getCategories(): Call<List<ApiCategory>>

    @POST("categories")
    fun addCategory(@Body request: CategoryRequest): Call<ApiCategory>

    @PUT("categories/{id}")
    fun updateCategory(@Path("id") id: Int, @Body request: CategoryRequest): Call<ApiCategory>

    @DELETE("categories/{id}")
    fun deleteCategory(@Path("id") id: Int): Call<ResponseBody>
}