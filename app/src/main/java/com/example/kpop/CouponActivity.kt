package com.example.kpop

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.adapter.CouponAdapter
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.api.UserCouponApi
import com.example.kpop.network.model.UserCoupon
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CouponActivity:AppCompatActivity(){

    private val api by lazy{RetrofitClient.create(this,UserCouponApi::class.java)}
    private lateinit var adapter:CouponAdapter
    private var selectMode=false

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coupons)


        selectMode=intent.getBooleanExtra("selectMode",false)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener{finish()}

        adapter=CouponAdapter(emptyList(),selectMode){coupon->
            if(selectMode)selectCoupon(coupon) else toggleCoupon(coupon)
        }

        findViewById<RecyclerView>(R.id.recyclerCoupons).apply{
            layoutManager=LinearLayoutManager(this@CouponActivity)
            adapter=this@CouponActivity.adapter
        }

        loadCoupons()
    }

    private fun loadCoupons(){
        api.getCoupons().enqueue(object:Callback<List<UserCoupon>>{
            override fun onResponse(call:Call<List<UserCoupon>>,response:Response<List<UserCoupon>>){
                if(!response.isSuccessful){
                    Toast.makeText(this@CouponActivity,"Unable to load coupons: ${response.code()}",Toast.LENGTH_LONG).show()
                    return
                }

                val coupons=response.body()?:emptyList()
                val displayList=if(selectMode)coupons.filter{it.saved} else coupons

                if(selectMode&&displayList.isEmpty())Toast.makeText(this@CouponActivity,"You have no saved vouchers",Toast.LENGTH_LONG).show()

                adapter.updateList(displayList)
            }

            override fun onFailure(call:Call<List<UserCoupon>>,t:Throwable){
                Toast.makeText(this@CouponActivity,"Server error: ${t.message}",Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun toggleCoupon(coupon:UserCoupon){
        val call=if(coupon.saved)api.removeCoupon(coupon.id) else api.saveCoupon(coupon.id)

        call.enqueue(object:Callback<ResponseBody>{
            override fun onResponse(call:Call<ResponseBody>,response:Response<ResponseBody>){
                if(!response.isSuccessful){
                    Toast.makeText(this@CouponActivity,response.errorBody()?.string()?:"Unable to update coupon",Toast.LENGTH_LONG).show()
                    return
                }

                Toast.makeText(this@CouponActivity,if(coupon.saved)"Voucher removed" else "Voucher saved",Toast.LENGTH_SHORT).show()
                loadCoupons()
            }

            override fun onFailure(call:Call<ResponseBody>,t:Throwable){
                Toast.makeText(this@CouponActivity,"Server error: ${t.message}",Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun selectCoupon(coupon:UserCoupon){
        val result=Intent()
        result.putExtra("couponId",coupon.id)
        result.putExtra("couponCode",coupon.code)
        result.putExtra("discountPercentage",coupon.discountPercentage)
        result.putExtra("minimumPurchase",coupon.minimumPurchase)
        result.putExtra("expiryDate",coupon.expiryDate)
        setResult(RESULT_OK,result)
        finish()
    }
}