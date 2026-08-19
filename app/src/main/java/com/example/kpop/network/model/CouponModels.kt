package com.example.kpop.network.model

data class ApiCoupon(
    val id:Int=0,
    val code:String="",
    val discountPercentage:Double=0.0,val minimumPurchase:Double=0.0,val expiryDate:String?=null,val active:Boolean=false)
data class CouponRequest(val code:String,val discountPercentage:Double,val minimumPurchase:Double,val expiryDate:String,val active:Boolean)