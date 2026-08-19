package com.example.kpop.network.api

import com.example.kpop.network.model.ApiReview
import com.example.kpop.network.model.ReviewEligibility
import com.example.kpop.network.model.ReviewRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReviewApi {

    @GET("reviews/product/{productId}")
    fun getReviews(
        @Path("productId") productId: Int
    ): Call<List<ApiReview>>

    @GET("reviews/eligibility/{productId}")
    fun checkEligibility(
        @Path("productId") productId: Int
    ): Call<ReviewEligibility>

    @POST("reviews")
    fun addReview(
        @Body request: ReviewRequest
    ): Call<ApiReview>
}