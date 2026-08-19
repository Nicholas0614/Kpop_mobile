package com.example.kpop.network.api

import com.example.kpop.network.model.AdminDashboard
import com.example.kpop.network.model.ApiOrder
import com.example.kpop.network.model.ApiProduct
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminApi {

    @GET("admin/dashboard")
    fun getDashboard(): Call<AdminDashboard>

    @GET("admin/orders")
    fun getOrders(): Call<List<ApiOrder>>

    @PUT("admin/orders/{id}/status")
    fun updateOrderStatus(@Path("id") id: Int, @Query("status") status: String): Call<ResponseBody>

    @PUT("admin/orders/{id}/tracking")
    fun updateTracking(@Path("id") id: Int, @Query("trackingNumber") trackingNumber: String): Call<ResponseBody>

    @GET("admin/products")
    fun getProducts(): Call<List<ApiProduct>>

    @PUT("admin/products/{id}/sale")
    fun updateProductSale(@Path("id") id: Int, @Query("onSale") onSale: Boolean, @Query("salePrice") salePrice: Double?): Call<ResponseBody>

    @PUT("admin/variants/{id}/sale")
    fun updateVariantSale(@Path("id") id: Int, @Query("onSale") onSale: Boolean, @Query("salePrice") salePrice: Double?): Call<ResponseBody>
}