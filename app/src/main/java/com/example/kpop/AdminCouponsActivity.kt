package com.example.kpop

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.adapter.AdminCouponAdapter
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.SessionManager
import com.example.kpop.network.api.CouponApi
import com.example.kpop.network.model.ApiCoupon
import com.example.kpop.network.model.CouponRequest
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class AdminCouponsActivity:AppCompatActivity(){

    private val couponApi by lazy{RetrofitClient.create(this,CouponApi::class.java)}
    private lateinit var adapter:AdminCouponAdapter

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_coupons)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.adminCoupon)){v,insets->
            val bars=insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left,bars.top,bars.right,bars.bottom)
            insets
        }

        if(!SessionManager(this).getRole().equals("admin",true)){finish();return}

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener{finish()}

        adapter=AdminCouponAdapter(emptyList(),{showCouponDialog(it)},{confirmDelete(it)})

        findViewById<RecyclerView>(R.id.recyclerCoupons).apply{
            layoutManager=LinearLayoutManager(this@AdminCouponsActivity)
            adapter=this@AdminCouponsActivity.adapter
        }

        findViewById<FloatingActionButton>(R.id.btnAddCoupon).setOnClickListener{showCouponDialog(null)}
        loadCoupons()
    }

    private fun loadCoupons(){
        couponApi.getCoupons().enqueue(object:Callback<List<ApiCoupon>>{
            override fun onResponse(call:Call<List<ApiCoupon>>,response:Response<List<ApiCoupon>>){
                if(response.isSuccessful)adapter.updateList(response.body()?:emptyList()) else Toast.makeText(this@AdminCouponsActivity,"Unable to load coupons",Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call:Call<List<ApiCoupon>>,t:Throwable){Toast.makeText(this@AdminCouponsActivity,"Server error: ${t.message}",Toast.LENGTH_LONG).show()}
        })
    }

    private fun showCouponDialog(coupon:ApiCoupon?){
        val layout=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(50,10,50,10)}
        val code=EditText(this).apply{hint="Coupon code";setText(coupon?.code?:"")}
        val discount=EditText(this).apply{hint="Discount %";inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL;setText(coupon?.discountPercentage?.toString()?:"")}
        val minimum=EditText(this).apply{hint="Minimum purchase";inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL;setText(coupon?.minimumPurchase?.toString()?:"")}
        val expiry=EditText(this).apply{hint="Expiry date";isFocusable=false;setText(coupon?.expiryDate?.substringBefore("T")?:"")}
        val active=CheckBox(this).apply{text="Active";isChecked=coupon?.active?:true}

        expiry.setOnClickListener{
            val calendar=Calendar.getInstance()
            DatePickerDialog(this,{_,year,month,day->expiry.setText("%04d-%02d-%02d".format(year,month+1,day))},calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        layout.addView(code);layout.addView(discount);layout.addView(minimum);layout.addView(expiry);layout.addView(active)

        val dialog=AlertDialog.Builder(this).setTitle(if(coupon==null)"Add Coupon" else "Edit Coupon").setView(layout).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create()

        dialog.setOnShowListener{
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{
                val couponCode=code.text.toString().trim().uppercase()
                val discountValue=discount.text.toString().toDoubleOrNull()
                val minimumValue=minimum.text.toString().toDoubleOrNull()
                val expiryValue=expiry.text.toString().trim()

                if(couponCode.isEmpty()){code.error="Enter coupon code";return@setOnClickListener}
                if(discountValue==null||discountValue<=0||discountValue>100){discount.error="Enter 1 - 100";return@setOnClickListener}
                if(minimumValue==null||minimumValue<0){minimum.error="Enter minimum purchase";return@setOnClickListener}
                if(expiryValue.isEmpty()){Toast.makeText(this,"Select expiry date",Toast.LENGTH_SHORT).show();return@setOnClickListener}

                saveCoupon(coupon,CouponRequest(couponCode,discountValue,minimumValue,"${expiryValue}T23:59:59",active.isChecked),dialog)
            }
        }

        dialog.show()
    }

    private fun saveCoupon(coupon:ApiCoupon?,request:CouponRequest,dialog:AlertDialog){
        val call=if(coupon==null)couponApi.addCoupon(request) else couponApi.updateCoupon(coupon.id,request)

        call.enqueue(object:Callback<ApiCoupon>{
            override fun onResponse(call:Call<ApiCoupon>,response:Response<ApiCoupon>){
                if(!response.isSuccessful){Toast.makeText(this@AdminCouponsActivity,response.errorBody()?.string()?:"Unable to save coupon",Toast.LENGTH_LONG).show();return}
                Toast.makeText(this@AdminCouponsActivity,if(coupon==null)"Coupon added" else "Coupon updated",Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                loadCoupons()
            }

            override fun onFailure(call:Call<ApiCoupon>,t:Throwable){Toast.makeText(this@AdminCouponsActivity,"Server error: ${t.message}",Toast.LENGTH_LONG).show()}
        })
    }

    private fun confirmDelete(coupon:ApiCoupon){
        AlertDialog.Builder(this).setTitle("Delete Coupon").setMessage("Delete ${coupon.code}?").setNegativeButton("Cancel",null).setPositiveButton("Delete"){_,_->deleteCoupon(coupon.id)}.show()
    }

    private fun deleteCoupon(id:Int){
        couponApi.deleteCoupon(id).enqueue(object:Callback<ResponseBody>{
            override fun onResponse(call:Call<ResponseBody>,response:Response<ResponseBody>){
                if(!response.isSuccessful){Toast.makeText(this@AdminCouponsActivity,response.errorBody()?.string()?:"Unable to delete coupon",Toast.LENGTH_LONG).show();return}
                Toast.makeText(this@AdminCouponsActivity,"Coupon deleted",Toast.LENGTH_SHORT).show()
                loadCoupons()
            }
            override fun onFailure(call:Call<ResponseBody>,t:Throwable){Toast.makeText(this@AdminCouponsActivity,"Server error: ${t.message}",Toast.LENGTH_LONG).show()}
        })
    }
}