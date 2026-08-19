package com.example.kpop.network.model

data class UserCoupon(val id:Int,val code:String,val discountPercentage:Double,val minimumPurchase:Double,val expiryDate:String,val saved:Boolean)