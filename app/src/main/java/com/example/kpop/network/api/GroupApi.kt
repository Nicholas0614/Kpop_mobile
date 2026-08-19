package com.example.kpop.network.api

import com.example.kpop.network.model.ApiGroup
import com.example.kpop.network.model.GroupRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface GroupApi {

    @GET("groups")
    fun getGroups(): Call<List<ApiGroup>>

    @POST("groups")
    fun addGroup(@Body request: GroupRequest): Call<ApiGroup>

    @PUT("groups/{id}")
    fun updateGroup(@Path("id") id: Int, @Body request: GroupRequest): Call<ApiGroup>

    @DELETE("groups/{id}")
    fun deleteGroup(@Path("id") id: Int): Call<ResponseBody>
}