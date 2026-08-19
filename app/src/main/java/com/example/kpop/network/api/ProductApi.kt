package com.example.kpop.network.api

import com.example.kpop.network.model.ApiProduct
import com.example.kpop.network.model.ApiProductImage
import com.example.kpop.network.model.ApiVariant
import com.example.kpop.network.model.ProductRequest
import com.example.kpop.network.model.VariantRequest
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {

    @GET("products")
    fun getProducts(): Call<List<ApiProduct>>

    @GET("products/{id}")
    fun getProduct(@Path("id") id: Int): Call<ApiProduct>

    @GET("products/search")
    fun searchProducts(
        @Query("keyword") keyword: String? = null,
        @Query("categoryId") categoryId: Int? = null,
        @Query("groupId") groupId: Int? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("sort") sort: String? = null
    ): Call<List<ApiProduct>>

    @POST("products")
    fun addProduct(@Body request: ProductRequest): Call<ApiProduct>

    @PUT("products/{id}")
    fun updateProduct(@Path("id") id: Int, @Body request: ProductRequest): Call<ApiProduct>

    @DELETE("products/{id}")
    fun deleteProduct(@Path("id") id: Int): Call<ResponseBody>

    @GET("variants/product/{productId}")
    fun getVariants(@Path("productId") productId: Int): Call<List<ApiVariant>>

    @POST("variants/product/{productId}")
    fun addVariant(@Path("productId") productId: Int, @Body request: VariantRequest): Call<ApiVariant>

    @PUT("variants/{id}")
    fun updateVariant(@Path("id") id: Int, @Body request: VariantRequest): Call<ApiVariant>

    @DELETE("variants/{id}")
    fun deleteVariant(@Path("id") id: Int): Call<ResponseBody>

    @GET("product-images/product/{productId}")
    fun getProductImages(@Path("productId") productId: Int): Call<List<ApiProductImage>>

    @Multipart
    @POST("product-images/product/{productId}/upload")
    fun uploadProductImage(@Path("productId") productId: Int, @Part file: MultipartBody.Part): Call<ApiProductImage>

    @DELETE("product-images/{id}")
    fun deleteProductImage(@Path("id") id: Int): Call<ResponseBody>
}